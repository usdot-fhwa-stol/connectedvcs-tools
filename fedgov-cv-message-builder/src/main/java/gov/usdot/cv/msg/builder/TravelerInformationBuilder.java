/*
 * Copyright (C) 2026 LEIDOS.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package gov.usdot.cv.msg.builder;

import java.nio.ByteBuffer;
import java.text.ParseException;
import gov.usdot.cv.msg.builder.exception.MessageEncodeException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import gov.usdot.cv.msg.builder.util.J2735TIMHelper;
import org.apache.logging.log4j.Logger;
import gov.usdot.cv.msg.builder.input.TravelerInputData.AnchorPoint;
import gov.usdot.cv.msg.builder.input.TravelerInputData.GenerateType;
import gov.usdot.cv.msg.builder.input.TravelerInputData.ItisContent;
import gov.usdot.cv.msg.builder.input.TravelerInputData.LaneNode;
import gov.usdot.cv.msg.builder.input.TravelerInputData.Region;
import gov.usdot.cv.timencoder.TravelerDataFrame.Content;
import io.netty.handler.codec.http2.Http2FrameLogger.Direction;
import gov.usdot.cv.msg.builder.util.BitStringHelper;
import gov.usdot.cv.msg.builder.util.GeoPoint;
import gov.usdot.cv.msg.builder.util.ItisNumberEncoder;
import gov.usdot.cv.mapencoder.NodeAttributeSetXY;
import gov.usdot.cv.mapencoder.NodeListXY;
import gov.usdot.cv.mapencoder.NodeOffsetPointXY;
import gov.usdot.cv.mapencoder.NodeSetXY;
import gov.usdot.cv.mapencoder.NodeXY;
import gov.usdot.cv.mapencoder.Position3D;
import gov.usdot.cv.msg.builder.exception.MessageBuildException;
import gov.usdot.cv.timencoder.*;
import gov.usdot.cv.timencoder.GeographicalPath.Description;
import gov.usdot.cv.timencoder.OffsetSystem.Offset;
import gov.usdot.cv.timencoder.OffsetSystem.Offset.Choice;
import gov.usdot.cv.msg.builder.input.IntersectionInputData;
import gov.usdot.cv.msg.builder.input.TravelerInputData;
import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import gov.usdot.cv.msg.builder.util.JSONMapper;
import gov.usdot.cv.msg.builder.util.ObjectPrinter;
import gov.usdot.cv.msg.builder.util.OffsetEncoding;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingSize;
import gov.usdot.cv.msg.builder.util.OffsetEncoding.OffsetEncodingType;
import gov.usdot.cv.msg.builder.message.SemiMessage;

@Path("/messages/travelerinfo")
public class TravelerInformationBuilder {

	private static final Logger logger = LogManager.getLogger(TravelerInformationBuilder.class);
	private static final SimpleDateFormat sdf;

	static {
		sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC")); 
	}

	private static final int MAX_MINUTES_DURATION = 32000; // DSRC spec

	private static final int LONG_BIT_STRING = 0b0000000000000000; // A 16-bit binary string
	private static final int LONG_BIT_STRING_LENGTH = 16; // Length of the 16-bit binary string

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public SemiMessage  build(String timData) {
		logger.debug("Building TIM/ADV with input data : " + timData);
		TravelerInformationMessage tim = new TravelerInformationMessage();
		SemiMessage semiMsg = null;
		TravelerInputData travInputData = null;
		boolean deposit = false;
		TravelerInformation ti = null;
		GenerateType generateType = GenerateType.TIM;

		System.out.println("Input JSON: " + timData);

		try {
			travInputData = JSONMapper.jsonStringToPojo(timData, TravelerInputData.class);

			deposit = (travInputData != null && travInputData.deposit != null);

			travInputData.validate();
			travInputData.applyLatLonOffset();
			travInputData.initialzeReferencePoints();
			System.out.println("Checking type : " + travInputData.getGenerateType());
			generateType = travInputData.getGenerateType();

		} catch (Exception e) {
			logger.error("Error parsing TravelerInputData ", e);
			throw new MessageBuildException(e.toString());
		}

		try {
			String hexString = "00";
			String readableString = "Unexpected type: " + generateType;

			switch (generateType) {
				case ASD:
					// TODO: ASD encoding will be implemented in a later story
					break;
				case TIM:
					semiMsg = new TravelerInformationMessage();
					ti = buildTravelerInformation(travInputData);
					String hexStringFull = J2735TIMHelper.getHexString(ti);
					// Strip MessageFrame header to extract TIM payload as the Header length varies due to ASN.1 length encoding:
					// short-form (≤127 bytes or < 0x80) uses 3 bytes → offset 6 hex chars, 
					// long-form (≥128 bytes or > 0x80) uses an extra length byte → offset 8.
					int firstLenByte = Integer.parseInt(hexStringFull.substring(4, 6), 16);
					int offset = (firstLenByte < 0x80) ? 6 : 8;
					hexString = hexStringFull.substring(offset);
					readableString = J2735TIMHelper.getReadableTIM(ti);
					break;
				case FramePlusTIM:
					semiMsg = new TravelerInformationMessage();
					ti = buildTravelerInformation(travInputData);
					hexString = J2735TIMHelper.getHexString(ti);
					readableString = J2735TIMHelper.getReadbaleTIMplusFrame(ti);
					break;
			}

			semiMsg.setHexString(hexString);
			System.out.println("TIM/ADV Hex: " + hexString);
			semiMsg.setReadableString(readableString);
		} catch (Exception e) {
			logger.error("Error encoding TravelerInformation ", e);
			throw new MessageEncodeException(e.toString());
		}

		return semiMsg;

	}

	private TravelerInformation buildTravelerInformation(TravelerInputData travInputData) throws ParseException {
		TravelerInformation tim = new TravelerInformation();
		tim.setDataFrames(buildDataFrames(travInputData));
		tim.setMsgCnt(tim.getDataFrames().getFrames().size());
		ByteBuffer buf = ByteBuffer.allocate(9).put((byte)0).putLong(travInputData.anchorPoint.packetID);
		tim.setPacketID(new UniqueMSGID(buf.array()));
		return tim;
	}

	private TravelerDataFrameList buildDataFrames(TravelerInputData travInputData) throws ParseException {
		TravelerDataFrameList dataFrames = new TravelerDataFrameList();
		TravelerDataFrame dataFrame = new TravelerDataFrame();

		// -- Part I, Frame header
		dataFrame.setDoNotUse1(new SSPindex(travInputData.anchorPoint.sspTimRights));

		dataFrame.setFrameType(TravelerInfoType.fromValue(travInputData.anchorPoint.infoType));
		dataFrame.setMsgId(getMessageId(travInputData));
		dataFrame.setStartYear(new DYear(getStartYear(travInputData))); //optional
		dataFrame.setStartTime(new MinuteOfTheYear(getStartTime(travInputData)));
		dataFrame.setDurationTime(new MinutesDuration(getDurationTime(travInputData)));
		dataFrame.setPriority(new SignPriority(travInputData.anchorPoint.priority));

		// -- Part II, Applicable Regions of Use
		dataFrame.setDoNotUse2(new SSPindex(travInputData.anchorPoint.sspTypeRights));
		// To Do: Build regions
		dataFrame.setRegions(buildRegions(travInputData));

		// -- Part III, Content
		dataFrame.setDoNotUse3(new SSPindex(travInputData.anchorPoint.sspContentRights)); // allowed message types
		dataFrame.setDoNotUse4(new SSPindex(travInputData.anchorPoint.sspLocationRights)); // allowed message content
		dataFrame.setContent(buildContent(travInputData));
		dataFrame.setContentNew(buiildContentNew(travInputData));

		dataFrames.addFrame(dataFrame);
		return dataFrames;
	}

	private Content buildContent(TravelerInputData travInputData) {
		String contentType = travInputData.anchorPoint.name;
		ItisContent[] codes = travInputData.anchorPoint.content;
		Content content = new Content();
		System.out.println("Content Type: " + contentType);
		if ("Advisory".equals(contentType)) {
			// takes max 100 itis codes or text with max length of 500 chars
			content.setAdvisory(buildAdvisory(codes, 100, 500));
		} else if ("Work Zone".equals(contentType)) {
			// takes max 16 itis codes or text with max length of 16 chars
			content.setWorkZone(buildWorkZone(codes, 16, 16));
		} else if ("Speed Limit".equals(contentType)) {
			// takes max 16 itis codes or text with max length of 16 chars
			content.setSpeedLimit(buildSpeedLimit(codes, 16, 16));
		} else if ("Exit Service".equals(contentType)) {
			// takes max 16 itis codes or text with max length of 16 chars
			content.setExitService(buildExitService(codes, 16, 16));
		} else {
			// takes max 16 itis codes or text with max length of 16 chars
			content.setGenericSign(buildGenericSignage(codes, 16, 16));
		}
		return content;
	}

	private TravelerDataFrameNewPartIIIContent buiildContentNew(TravelerInputData travInputData) {
		TravelerDataFrameNewPartIIIContent contentNew = new TravelerDataFrameNewPartIIIContent();
		FrictionInformation frictionInformation = buildFrictionInformation(travInputData);
		contentNew.setFrictionInformation(frictionInformation);
		return contentNew;
	}

	private ITIScodesAndText buildAdvisory(ItisContent[] codes, final int sizeLimit, final int textLengthLimit) {
		ITIScodesAndText advisory = new ITIScodesAndText();
		if (codes == null || sizeLimit <= 0)
			return advisory;

		List<ITIScodesAndText.Item> items = advisory.getItems();
		if (items == null) {
			items = new ArrayList<>();
			advisory.setItems(items);
		}

		for (ItisContent code : codes) {
			if (code == null)
				continue;

			if (code.hasText()) {
				ITISTextPhrase text = new ITISTextPhrase(code.getText(textLengthLimit));
				items.add(new ITIScodesAndText.Item(text));
				if (items.size() >= sizeLimit)
					break;

			} else if (code.hasCodes() && code.codes != null) {

				int[] itisCodes = ItisNumberEncoder.encode(code.codes);
				for (int itisCode : itisCodes) {
					items.add(new ITIScodesAndText.Item(new ITIScodes(itisCode)));
					if (items.size() >= sizeLimit)
						break;
				}
			}
		}
		return advisory;
	}

	private WorkZone buildWorkZone(ItisContent[] codes, final int sizeLimit, final int textLengthLimit) {
		WorkZone wz = new WorkZone();
		if (codes == null || sizeLimit <= 0)
			return wz;
		List<WorkZone.WorkZoneItem> items = wz.getItems();
		if (items == null) {
			items = new ArrayList<>();
			wz.setItems(items);
		}
		for (ItisContent code : codes) {
			if (code == null)
				continue;

			if (code.hasText()) {
				ITISTextPhrase text = new ITISTextPhrase(code.getText(textLengthLimit));
				items.add(new WorkZone.WorkZoneItem(text));
				if (items.size() >= sizeLimit)
					break;

			} else if (code.hasCodes() && code.codes != null) {
				int[] itisCodes = ItisNumberEncoder.encode(code.codes);
				for (int itisCode : itisCodes) {
					items.add(new WorkZone.WorkZoneItem(new ITIScodes(itisCode)));
					if (items.size() >= sizeLimit)
						break;
				}
			}
		}
		return wz;
	}

	private SpeedLimit buildSpeedLimit(ItisContent[] codes, final int sizeLimit, final int textLengthLimit) {
		SpeedLimit sl = new SpeedLimit();
		if (codes == null || sizeLimit <= 0)
			return sl;

		List<SpeedLimit.SpeedLimitItem> items = sl.getItems();
		if (items == null) {
			items = new ArrayList<>();
			sl.setItems(items);
		}

		for (ItisContent code : codes) {
			if (code == null)
				continue;

			if (code.hasText()) {
				ITISTextPhrase text = new ITISTextPhrase(code.getText(textLengthLimit));
				items.add(new SpeedLimit.SpeedLimitItem(text));
				if (items.size() >= sizeLimit)
					break;

			} else if (code.hasCodes() && code.codes != null) {
				int[] itisCodes = ItisNumberEncoder.encode(code.codes);
				for (int itisCode : itisCodes) {
					items.add(new SpeedLimit.SpeedLimitItem(new ITIScodes(itisCode)));
					if (items.size() >= sizeLimit)
						break;
				}
			}
		}
		return sl;
	}

	private ExitService buildExitService(ItisContent[] codes, final int sizeLimit, final int textLengthLimit) {
		ExitService es = new ExitService();
		if (codes == null || sizeLimit <= 0)
			return es;

		List<ExitService.ExitServiceItem> items = es.getItems();
		if (items == null) {
			items = new ArrayList<>();
			es.setItems(items);
		}

		for (ItisContent code : codes) {
			if (code == null)
				continue;

			if (code.hasText()) {
				ITISTextPhrase text = new ITISTextPhrase(code.getText(textLengthLimit)); // or ITIStextPhrase
				items.add(new ExitService.ExitServiceItem(text));
				if (items.size() >= sizeLimit)
					break;

			} else if (code.hasCodes() && code.codes != null) {
				int[] itisCodes = ItisNumberEncoder.encode(code.codes); // or: code.codes
				for (int itisCode : itisCodes) {
					items.add(new ExitService.ExitServiceItem(new ITIScodes(itisCode)));
					if (items.size() >= sizeLimit)
						break;
				}
			}
		}
		return es;
	}

	private GenericSignage buildGenericSignage(ItisContent[] codes, final int sizeLimit, final int textLengthLimit) {
		GenericSignage gs = new GenericSignage();
		if (codes == null || sizeLimit <= 0)
			return gs;

		List<GenericSignage.GenericSignageItem> items = gs.getItems();
		if (items == null) {
			items = new ArrayList<>();
			gs.setItems(items);
		}

		for (ItisContent code : codes) {
			if (code == null)
				continue;

			if (code.hasText()) {
				ITISTextPhrase text = new ITISTextPhrase(code.getText(textLengthLimit));
				items.add(new GenericSignage.GenericSignageItem(text));
				if (items.size() >= sizeLimit)
					break;

			} else if (code.hasCodes() && code.codes != null) {
				int[] itisCodes = ItisNumberEncoder.encode(code.codes);
				for (int itisCode : itisCodes) {
					items.add(new GenericSignage.GenericSignageItem(new ITIScodes(itisCode)));
					if (items.size() >= sizeLimit)
						break;
				}
			}
		}
		return gs;
	}

	private long getStartTime(TravelerInputData travInputData) throws ParseException {
		Date startDate = sdf.parse(travInputData.anchorPoint.startTime);
		String startOfYearTime = "01/01/" + getStartYear(travInputData) + " 12:00 AM";
		Date startOfYearDate = sdf.parse(startOfYearTime);
		long minutes = ((startDate.getTime() - startOfYearDate.getTime()) / 60000) + 1440;
		return minutes;
	}

	private int getStartYear(TravelerInputData travInputData) throws ParseException {
		Date startDate = sdf.parse(travInputData.anchorPoint.startTime);
		Calendar cal = Calendar.getInstance();
		cal.setTime(startDate);
		return cal.get(Calendar.YEAR);
	}

	private int getDurationTime(TravelerInputData travInputData) throws ParseException {
		Date startDate = sdf.parse(travInputData.anchorPoint.startTime);
		Date endDate = sdf.parse(travInputData.anchorPoint.endTime);

		long diff = endDate.getTime() - startDate.getTime();
		int durationInMinutes = (int) (diff / 1000 / 60);
		if (durationInMinutes > MAX_MINUTES_DURATION)
			durationInMinutes = MAX_MINUTES_DURATION;
		return durationInMinutes;
	}

	private MsgId getMessageId(TravelerInputData travInputData) {
		MsgId msgId = new MsgId();
		// always using RoadSign for now
		boolean isRoadSign = true;
		if (isRoadSign) {
			RoadSignID roadSignID = new RoadSignID();
			roadSignID.setPosition(getAnchorPointPosition(travInputData));
			roadSignID.setViewAngle(getHeadingSlice(travInputData));
			roadSignID.setMutcdCode(getMutcdFromInt(travInputData.anchorPoint.mutcd));
			msgId.setRoadSignID(roadSignID);
		} else {
			// FurtherInfoID is not supported by the Legacy tool using a dummy value here as
			// well
			msgId.setFurtherInfoID(new FurtherInfoID(new byte[] { 0x00, 0x00 }));
		}
		return msgId;
	}

	private static Position3D getAnchorPointPosition(TravelerInputData travInputData) {
		AnchorPoint anchorPoint = travInputData.anchorPoint;
		assert (anchorPoint != null);
		Position3D anchorPos = new Position3D();
		anchorPos.setLongitude(J2735TIMHelper.convertGeoCoordinateToInt(anchorPoint.referenceLon));
		anchorPos.setLatitude(J2735TIMHelper.convertGeoCoordinateToInt(anchorPoint.referenceLat));
		if (travInputData.enableElevation) {
			anchorPos.setElevationExists(true);
			anchorPos.setElevation((float) anchorPoint.getReferenceElevation());
		}
		return anchorPos;
	}

	private HeadingSlice getHeadingSlice(TravelerInputData travInputData) {
		int[] heading = travInputData.anchorPoint.heading;

		// If heading is null or empty, return headingSlide as 0
		if (heading == null || heading.length == 0) {
			return new HeadingSlice(0);
		}

		int headingSliceBitString = LONG_BIT_STRING;
		int headingBitString = BitStringHelper.getBitString(headingSliceBitString, LONG_BIT_STRING_LENGTH, heading);
		HeadingSlice headingSlice = new HeadingSlice(headingBitString);

		return headingSlice;
	}

	private List<GeographicalPath> buildRegions(TravelerInputData travInputData) {
		List<GeographicalPath> regionList = new ArrayList<>();

		OffsetEncoding offsetEncoding = new OffsetEncoding(travInputData.nodeOffsets);

		if (offsetEncoding.type != OffsetEncodingType.Tight) {
			offsetEncoding.size = getOffsetEncodingSize(offsetEncoding.type, travInputData.regions, travInputData.anchorPoint);
		}

		for (Region inputRegion : travInputData.regions) {
			GeographicalPath geoPath = new GeographicalPath();

			geoPath.setAnchor(getAnchorPointPosition(travInputData));

			if (travInputData.anchorPoint.direction != 0) {
				// Our values for direction are off by 1 since the GUI doesn't support
				// "unavailable" DirectionOfUse
				int directionOfUse = travInputData.anchorPoint.direction;// + 1;
				geoPath.setDirectionality(DirectionOfUse.valueOf(directionOfUse));
			}

			Description description = new Description();
			if (inputRegion.regionType.equals("circle")) {
				GeometricProjection geometry = new GeometricProjection();

				geometry.setHeadingSlice(getHeadingSlice(travInputData));

				geometry.setExtent(Extent.fromValue(inputRegion.extent));

				if (travInputData.anchorPoint.masterLaneWidth != 0) {
					geometry.setLaneWidth((int) travInputData.anchorPoint.masterLaneWidth);
				}

				geometry.setCircle(buildCircle(travInputData, inputRegion));

				description.setGeometry(geometry);
			} else {
				if (travInputData.anchorPoint.masterLaneWidth != 0) {
					geoPath.setLaneWidth((int) travInputData.anchorPoint.masterLaneWidth);
				}

				geoPath.setDirection(getHeadingSlice(travInputData));

				if (inputRegion.regionType.equals("lane")) {
					geoPath.setClosedPath(false);
				} else if (inputRegion.regionType.equals("region")) {
					geoPath.setClosedPath(true);
				}

				OffsetSystem path = new OffsetSystem();
				path.setOffset(buildOffset(travInputData, inputRegion, offsetEncoding));
				description.setPath(path);
			}
			geoPath.setDescription(description);
			regionList.add(geoPath);
		}
		return regionList;
	}

	private OffsetEncodingSize getOffsetEncodingSize(OffsetEncodingType offsetEncodingType, Region[] regions,
			TravelerInputData.AnchorPoint anchorPoint) {
		OffsetEncodingSize offsetEncodingSize;

		switch (offsetEncodingType) {
			case Compact:
				int longestRegionOffsetInCm = 0;
				for (Region inputRegion : regions) {
					int longestLaneOffsetInCm = Math.abs(getLongestOffsetDistanceInCm(anchorPoint, inputRegion.laneNodes));
					if (longestLaneOffsetInCm > longestRegionOffsetInCm) {
						longestRegionOffsetInCm = longestLaneOffsetInCm;
					}
				}

				offsetEncodingSize = OffsetEncodingSize.getOffsetEncodingSize(longestRegionOffsetInCm);
				break;
			case Explicit:
				offsetEncodingSize = OffsetEncodingSize.Explicit64Bit;
				break;
			case Standard:
			default:
				offsetEncodingSize = OffsetEncodingSize.Offset32Bit;
				break;
		}

		return offsetEncodingSize;
	}

	private int getLongestOffsetDistanceInCm(TravelerInputData.AnchorPoint anchorPoint, LaneNode[] laneNodes) {
		int longestDistanceInCm = 0;

		try {
			// Check the offset from the reference point first
			GeoPoint gpReference = new GeoPoint(anchorPoint.referenceLat, anchorPoint.referenceLon);
			GeoPoint gpFirstNode = new GeoPoint(laneNodes[0].nodeLat, laneNodes[0].nodeLong);

			int longerOffset = OffsetEncoding.getLongerOffset(gpReference, gpFirstNode);
			if (longerOffset > longestDistanceInCm) {
				longestDistanceInCm = longerOffset;
			}

			// Work through the rest of the points in the lane
			for (int i = 0; i < laneNodes.length - 1; i++) {
				GeoPoint gp1 = new GeoPoint(laneNodes[i].nodeLat, laneNodes[i].nodeLong);
				GeoPoint gp2 = new GeoPoint(laneNodes[i + 1].nodeLat, laneNodes[i + 1].nodeLong);

				longerOffset = OffsetEncoding.getLongerOffset(gp1, gp2);
				if (longerOffset > longestDistanceInCm) {
					longestDistanceInCm = longerOffset;
				}
			}
		} catch (IllegalArgumentException e) {
			// If we catch this, the length is CM is larger than fits in a short.
			// Currently the standard only supports up to what fits in a short for
			// offset distances. An explicit Lat/Lon value will have to be used.
			// Return a value greater than the short max value
			longestDistanceInCm = Short.MAX_VALUE + 1;
		}

		return longestDistanceInCm;
	}

	private Circle buildCircle(TravelerInputData travInputData, TravelerInputData.Region inputRegion) {
		Circle circle = new Circle();

		Position3D center = new Position3D();
		center.setLatitude(J2735TIMHelper
				.convertGeoCoordinateToInt(inputRegion.laneNodes[TravelerInputData.LaneNode.circleCenter].nodeLat));
		center.setLongitude(J2735TIMHelper
				.convertGeoCoordinateToInt(inputRegion.laneNodes[TravelerInputData.LaneNode.circleCenter].nodeLong));

		if (travInputData.enableElevation) {
			center.setElevationExists(true);
			center.setElevation(IntersectionInputData.convertElevation(inputRegion.laneNodes[TravelerInputData.LaneNode.circleCenter].nodeElevation));
		}
		circle.setCenter(center);

		setRadiusAndUnits(circle, inputRegion.radius);

		return circle;
	}

	private void setRadiusAndUnits(Circle circle, double radiusInMeters) {
		double radiusInCentimeters = radiusInMeters * 100;

		double radius;
		DistanceUnits distanceUnits;
		if (0 <= radiusInCentimeters && radiusInCentimeters <= 4094) {
			radius = radiusInCentimeters;
			distanceUnits = DistanceUnits.centimeter;
		} else if (4095 <= radiusInCentimeters && radiusInCentimeters <= 40940) {
			radius = radiusInCentimeters / 10;
			distanceUnits = DistanceUnits.decimeter;
		} else if (40941 <= radiusInCentimeters && radiusInCentimeters <= 409400) {
			radius = radiusInCentimeters / 100;
			distanceUnits = DistanceUnits.meter;
		} else if (409401 <= radiusInCentimeters && radiusInCentimeters <= 409400000) {
			radius = radiusInCentimeters / 100000;
			distanceUnits = DistanceUnits.kilometer;
		} else {
			// Value of 4095 indicates unknown.
			radius = 4095;
			distanceUnits = DistanceUnits.centimeter;
		}
		int roundedRadius = (int) Math.ceil(radius);

		// No need to waste space with trailing 0s, i.e., why have 400 cm when that is 4m.
		// Up-convert the distance unit and radius when possible.
		if (distanceUnits == DistanceUnits.centimeter) {
			int decimeterValue = roundedRadius / 10;
			if (decimeterValue * 10 == roundedRadius) {
				roundedRadius = decimeterValue;
				distanceUnits = DistanceUnits.decimeter;
			}
		}
		if (distanceUnits == DistanceUnits.decimeter) {
			int meterValue = roundedRadius / 10;
			if (meterValue * 10 == roundedRadius) {
				roundedRadius = meterValue;
				distanceUnits = DistanceUnits.meter;
			}
		}
		if (distanceUnits == DistanceUnits.meter) {
			int kilometerValue = roundedRadius / 1000;
			if (kilometerValue * 1000 == roundedRadius) {
				roundedRadius = kilometerValue;
				distanceUnits = DistanceUnits.kilometer;
			}
		}

		circle.setRadius(new Radius_B12(roundedRadius));
		circle.setUnits(distanceUnits);
	}

	private NodeListXY buildNodeList(TravelerInputData travInputData, Region inputRegion, OffsetEncoding offsetEncoding) {
		LaneNode[] laneNodes = inputRegion.laneNodes;

		NodeListXY nodeList = new NodeListXY();
		nodeList.setChoice(NodeListXY.NODE_SET_XY);

		NodeSetXY nodeSetXY = new NodeSetXY();
		double curElevation = travInputData.anchorPoint.referenceElevation;
		GeoPoint refPoint = inputRegion.refPoint;

		NodeXY[] nodeXyArray = new NodeXY[laneNodes.length];
		int nodeIndex = 0;

		for (int i = 0; i < laneNodes.length; i++) {
			LaneNode laneNode = laneNodes[i];

			GeoPoint nextPoint = new GeoPoint(laneNode.nodeLat, laneNode.nodeLong);

			if (offsetEncoding.type == OffsetEncodingType.Tight) {
				offsetEncoding.size = offsetEncoding.getOffsetEncodingSize(refPoint, nextPoint);
			}

			// Get the Node Offset
			NodeOffsetPointXY delta = offsetEncoding.encodeOffset(refPoint, nextPoint);
			NodeXY nodeXy = new NodeXY();
			nodeXy.setDelta(delta);

			// Set Node Attributes
			NodeAttributeSetXY attributes = new NodeAttributeSetXY();
			boolean hasAttributes = false;

			// Set dWidth
			if (laneNode.laneWidth != 0) {
				attributes.setDWidthExists(true);
				attributes.setDWidth(laneNode.laneWidth);
				hasAttributes = true;
			}

			// Set dElevation
			if (travInputData.enableElevation) {
				short elevDelta = IntersectionSituationDataBuilder.getElevationDelta(laneNode.nodeElevation,
						curElevation);
				if (elevDelta != 0) {
					curElevation = laneNode.nodeElevation;
					attributes.setDElevationExists(true);
					attributes.setDElevation(elevDelta);
					hasAttributes = true;
				}
			}

			if (hasAttributes) {
				nodeXy.setAttributesExists(true);
				nodeXy.setAttributes(attributes);
			}

			nodeXyArray[nodeIndex] = nodeXy;
			nodeIndex++;
			refPoint = nextPoint;
		}
		nodeSetXY.setNodeSetXY(nodeXyArray);
		nodeList.setNodes(nodeSetXY);
		return nodeList;
	}

	private Offset buildOffset(TravelerInputData travInputData, Region inputRegion, OffsetEncoding offsetEncoding) {
		Offset offset = new Offset();
		offset.setChoice(Choice.xy_chosen);
		offset.setXy_chosen(buildNodeList(travInputData, inputRegion, offsetEncoding));

		return offset;
	}

	private FrictionInformation buildFrictionInformation(TravelerInputData travInputData) {
		FrictionInformation fInformation = new FrictionInformation();
		DescriptionOfRoadSurface roadSurface = null;
		boolean hasData = false;

		String surfaceValue = travInputData.anchorPoint.road_surface;
		int type_value = travInputData.anchorPoint.road_surface_type;

		if (surfaceValue != null) {

			switch (surfaceValue.toLowerCase()) {
				case "portland cement":
					roadSurface = new DescriptionOfRoadSurface(
							new PortlandCement(PortlandCementType.fromInt(type_value)));
					break;
				case "asphalt or tar":
					roadSurface = new DescriptionOfRoadSurface(new AsphaltOrTar(AsphaltOrTarType.fromInt(type_value)));
					break;
				case "gravel":
					roadSurface = new DescriptionOfRoadSurface(new Gravel(GravelType.fromInt(type_value)));
					break;
				case "grass":
					roadSurface = new DescriptionOfRoadSurface(new Grass(GrassType.fromInt(type_value)));
					break;
				case "cinders":
					roadSurface = new DescriptionOfRoadSurface(new Cinders(CindersType.fromInt(type_value)));
					break;
				case "rock":
					roadSurface = new DescriptionOfRoadSurface(new Rock(RockType.fromInt(type_value)));
					break;
				case "ice":
					roadSurface = new DescriptionOfRoadSurface(new Ice(IceType.fromInt(type_value)));
					break;
				case "snow":
					roadSurface = new DescriptionOfRoadSurface(new Snow(SnowType.fromInt(type_value)));
					break;
				default:
					break;
			}
		}
		if (roadSurface != null) {
			fInformation.setRoadSurfaceDescription(roadSurface);
			hasData = true;
		}

		String dry_wet_value = travInputData.anchorPoint.road_condition;
		if (dry_wet_value!= null) {
			int dry_wet= Integer.parseInt(dry_wet_value);
			fInformation.setDryOrWet(RoadSurfaceCondition.fromInt(dry_wet));
			hasData = true;
		}

		long meanHorizontalVariationValue = travInputData.anchorPoint.meanHorizontalVariation;
		long stdevHorizontalVariationValue = travInputData.anchorPoint.horizontalVariationStdDev;
		long meanVerticalVariationValue = travInputData.anchorPoint.meanVerticalVariation;
		long stdevVerticalVariationValue = travInputData.anchorPoint.verticalVariationStdDev;

		boolean hasRoughness = meanHorizontalVariationValue != 0 || stdevHorizontalVariationValue != 0 ||
				meanVerticalVariationValue != 0 || stdevVerticalVariationValue != 0;

		if (hasRoughness) {
			RoadRoughness roadRoughness = new RoadRoughness();
			roadRoughness.setMeanHorizontalVariation(new CommonMeanVariation(meanHorizontalVariationValue));
			roadRoughness.setHorizontalVariationStdDev(new VariationStdDev(stdevHorizontalVariationValue));
			roadRoughness.setMeanVerticalVariation(new CommonMeanVariation(meanVerticalVariationValue));
			roadRoughness.setVerticalVariationStdDev(new VariationStdDev(stdevVerticalVariationValue));

			fInformation.setRoadRoughness(roadRoughness);
			hasData = true;
		}

		return hasData ? fInformation : null;
	}


	public static MUTCDCode getMutcdFromInt(int value) {
		switch (value) {
			case 0:
				return MUTCDCode.none;
			case 1:
				return MUTCDCode.regulatory;
			case 2:
				return MUTCDCode.warning;
			case 3:
				return MUTCDCode.maintenance;
			case 4:
				return MUTCDCode.motoristService;
			case 5:
				return MUTCDCode.guide;
			case 6:
				return MUTCDCode.rec;
			default:
				return MUTCDCode.none;
		}
	}
	
}