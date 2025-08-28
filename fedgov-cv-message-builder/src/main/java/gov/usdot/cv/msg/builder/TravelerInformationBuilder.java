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
import java.nio.ByteBuffer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import gov.usdot.cv.msg.builder.exception.MessageBuildException;
import gov.usdot.cv.msg.builder.input.IntersectionInputData;
import gov.usdot.cv.timencoder.MinuteOfTheYear;
import gov.usdot.cv.timencoder.MinutesDuration;
import gov.usdot.cv.timencoder.SSPindex;
import gov.usdot.cv.timencoder.TravelerDataFrame;
import gov.usdot.cv.timencoder.TravelerDataFrameList;
import gov.usdot.cv.timencoder.TravelerInfoType;
import gov.usdot.cv.timencoder.TravelerInformation;
import gov.usdot.cv.msg.builder.input.TravelerInputData;
import gov.usdot.cv.msg.builder.message.SemiMessage;
import gov.usdot.cv.msg.builder.message.TravelerInformationMessage;
import gov.usdot.cv.msg.builder.util.J2735Helper;
import gov.usdot.cv.msg.builder.util.JSONMapper;


@Path("/messages/travelerinfo")
public class TravelerInformationBuilder {
    
    private static final Logger logger = LogManager.getLogger(IntersectionSituationDataBuilder.class);

    @POST
	@Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.TEXT_PLAIN)
	public TravelerInformationMessage build(String timData) {
        logger.debug("Building TIM/ADV with input data : " + timData);
		TravelerInformationMessage tim = new TravelerInformationMessage();
        TravelerInputData travInputData = null;
		boolean deposit = false;
		SemiMessage semi = null;
		TravelerInformation ti = null;
        /* 
        MessageFrame mf = null;
		GenerateType generateType = GenerateType.TIM;
        */
        travInputData.applyLatLonOffset();
		travInputData.initialzeReferencePoints();
		//generateType = travInputData.getGenerateType();
		//ti = buildTravelerInformation(travInputData);

        try {
			travInputData = JSONMapper.jsonStringToPojo(timData, TravelerInputData.class);
            deposit = (travInputData != null && travInputData.deposit != null);
           
            //TODO:
            //validate is commented out will investigate later
            //travInputData.validate();
            travInputData.applyLatLonOffset();
			travInputData.initialzeReferencePoints();
			//generateType = travInputData.getGenerateType();

			ti = buildTravelerInformation(travInputData);


            

		} catch (Exception e) {
			logger.error("Error parsing TravelerInputData ", e);
			throw new MessageBuildException(e.toString());
		}
        /* Place Holder will implement later */

        return tim;

    }

    private TravelerInformation buildTravelerInformation(TravelerInputData travInputData) {
		TravelerInformation tim = new TravelerInformation();
		tim.setDataFrames(buildDataFrames(travInputData));
		//tim.setMsgCnt(tim.getDataFrames().getSize());
        //Todo: will replace with method from tim Dataframe Size
        tim.setMsgCnt(1000);
		ByteBuffer buf = ByteBuffer.allocate(9).put((byte)0).putLong(travInputData.anchorPoint.packetID);
		//Unique MsgID is optional and will implement in a later story
       // tim.setPacketID(new UniqueMSGID(buf.array()));
		return tim;
	}

    private TravelerDataFrameList buildDataFrames(TravelerInputData travInputData) {
		TravelerDataFrameList dataFrames = new TravelerDataFrameList();
		TravelerDataFrame dataFrame = new TravelerDataFrame();
		
		// -- Part I, Frame header
		dataFrame.setDoNotUse1(new SSPindex(travInputData.anchorPoint.sspTimRights));
		dataFrame.setFrameType(new TravelerInfoType());
		dataFrame.setMsgId(getMessageId(travInputData));
		dataFrame.setStartYear(new DYear(getStartYear(travInputData)));
		dataFrame.setStartTime(new MinuteOfTheYear(getStartTime(travInputData)));
		dataFrame.setDuratonTime(new MinutesDuration(getDurationTime(travInputData)));
		dataFrame.setPriority(new SignPrority(travInputData.anchorPoint.priority));
		
		// -- Part II, Applicable Regions of Use
		dataFrame.setSspLocationRights(new SSPindex(travInputData.anchorPoint.sspLocationRights));
		dataFrame.setRegions(buildRegions(travInputData));
		
		// -- Part III, Content
		dataFrame.setSspMsgRights1(new SSPindex(travInputData.anchorPoint.sspTypeRights));		// allowed message types
		dataFrame.setSspMsgRights2(new SSPindex(travInputData.anchorPoint.sspContentRights));	// allowed message content	
		dataFrame.setContent(buildContent(travInputData));
		
		dataFrames.add(dataFrame);
		return dataFrames;
	}
}
