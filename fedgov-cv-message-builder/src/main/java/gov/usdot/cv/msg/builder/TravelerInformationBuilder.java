/*
 * Copyright (C) 2025 LEIDOS.
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
import java.text.ParseException;
import gov.usdot.cv.msg.builder.exception.MessageEncodeException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import gov.usdot.cv.timencoder.TravelerDataFrame.Content;
import gov.usdot.cv.msg.builder.util.ItisNumberEncoder;
import gov.usdot.cv.mapencoder.Position3D;
import gov.usdot.cv.msg.builder.exception.MessageBuildException;
import gov.usdot.cv.timencoder.*;
import gov.usdot.cv.msg.builder.input.TravelerInputData;
import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import gov.usdot.cv.msg.builder.util.JSONMapper;


@Path("/messages/travelerinfo")
public class TravelerInformationBuilder {

	private static final Logger logger = LogManager.getLogger(TravelerInformationBuilder.class);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
	private static final int MAX_MINUTES_DURATION = 32000; // DSRC spec

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public TravelerInformationMessage build(String timData) {
		logger.debug("Building TIM/ADV with input data : " + timData);
		TravelerInformationMessage tim = new TravelerInformationMessage();
		TravelerInputData travInputData = null;
		boolean deposit = false;
		TravelerInformation ti = null;
		GenerateType generateType = GenerateType.TIM;

		System.out.println("Input JSON: " + timData);

		try {
			travInputData = JSONMapper.jsonStringToPojo(timData, TravelerInputData.class);

			deposit = (travInputData != null && travInputData.deposit != null);

			// TODO:
			// validate is commented out will investigate later
			// travInputData.validate();
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
					//TODO: ASD encoding will be implemented in a later story
					break;
				case TIM:
					tim = new TravelerInformationMessage();
					ti = buildTravelerInformation(travInputData);
					hexString = J2735TIMHelper.getHexString(ti).substring(6);
					readableString = ti.toString();
					break;
				case FramePlusTIM:
					tim = new TravelerInformationMessage();
					ti = buildTravelerInformation(travInputData);
					hexString = J2735TIMHelper.getHexString(ti);
					readableString = ti.toString();
					break;
			}

			tim.setHexString(hexString);
			System.out.println("TIM/ADV Hex: " + hexString);
			tim.setReadableString(readableString);
		} catch (Exception e) {
			logger.error("Error encoding TravelerInformation ", e);
			throw new MessageEncodeException(e.toString());
		}

		return tim;

	}

	private TravelerInformation buildTravelerInformation(TravelerInputData travInputData) throws ParseException {
		TravelerInformation tim = new TravelerInformation();
		tim.setDataFrames(buildDataFrames(travInputData));
		tim.setMsgCnt(tim.getDataFrames().getFrames().size());
		return tim;
	}

	private TravelerDataFrameList buildDataFrames(TravelerInputData travInputData) throws ParseException {
		TravelerDataFrameList dataFrames = new TravelerDataFrameList();
		TravelerDataFrame dataFrame = new TravelerDataFrame();

		// -- Part I, Frame header
		dataFrame.setDoNotUse1(new SSPindex(travInputData.anchorPoint.sspTimRights));

		dataFrame.setFrameType(TravelerInfoType.fromValue(travInputData.anchorPoint.infoType));
		dataFrame.setMsgId(getMessageId(travInputData));
		// TODO:StartYear is an Optional field will be implemented later
		// dataFrame.setStartYear(new DYear(getStartYear(travInputData))); //optional
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
			//takes max 100 itis codes or text with max length of 500 chars
			content.setAdvisory(buildAdvisory(codes, 100, 500));
		} else if ("Work Zone".equals(contentType)) {
			//takes max 16 itis codes or text with max length of 16 chars
			content.setWorkZone(buildWorkZone(codes, 16, 16));
		} else if ("Speed Limit".equals(contentType)) {
			//takes max 16 itis codes or text with max length of 16 chars
			content.setSpeedLimit(buildSpeedLimit(codes, 16, 16));
		} else if ("Exit Service".equals(contentType)) {
			//takes max 16 itis codes or text with max length of 16 chars
			content.setExitService(buildExitService(codes, 16, 16));
		} else {
			//takes max 16 itis codes or text with max length of 16 chars
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
			//Todo: 
			// MUTCD code is an optional field will be implement later story
			// roadSignID.setMutcdCode(MUTCDCode.valueOf(travInputData.anchorPoint.mutcd));
			msgId.setRoadSignID(roadSignID);
		} else {
			// FurtherInfoID is not supported by the Legacy tool using a dummy value here as well
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
			anchorPos.setElevation((float) anchorPoint.referenceElevation);
		}
		return anchorPos;
	}

	private HeadingSlice getHeadingSlice(TravelerInputData travInputData) {
		int[] heading = travInputData.anchorPoint.heading;
		HeadingSlice headingSlice = new HeadingSlice();
		if (heading != null && heading.length != 0) {
			for (int i = 0; i < heading.length; i++) {
				headingSlice.set(i, heading[i] == 1);
			}
		}

		return headingSlice;
	}

	private GeographicalPath[] buildRegions(TravelerInputData travInputData) {
		List<GeographicalPath> regionList = new ArrayList<>();
		GeographicalPath region1 = new GeographicalPath();
		// TODO: will be implemented in a  later story
		regionList.add(region1);
		return regionList.toArray(new GeographicalPath[0]);
	}

	private FrictionInformation buildFrictionInformation(TravelerInputData travInputData) {
		FrictionInformation fInformation = new FrictionInformation();
		DescriptionOfRoadSurface roadSurface = new DescriptionOfRoadSurface();
		//

		//ToDo: Inputs of surface condition will be implemented in a later story
		String surfaceValue = "portland_cement"; 

		int type_value = 1; 
		switch (surfaceValue.toLowerCase()) {
			case "portland_cement":
				roadSurface = new DescriptionOfRoadSurface(new PortlandCement(PortlandCementType.fromInt(0)));
				break;
			case "asphalt_or_tar":
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

		fInformation.setRoadSurfaceDescription(roadSurface);

		//Todo:will get from travinput in a later story
		int dry_wet_value = 1; 
		fInformation.setDryOrWet(RoadSurfaceCondition.fromInt(dry_wet_value));

		return fInformation;
	}
}