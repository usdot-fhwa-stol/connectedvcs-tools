/*
 * Copyright (C) 2024 LEIDOS.
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
package gov.usdot.cv.bingmap.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/***
 * @brief Definition of ImageryMetadata refer to: https://learn.microsoft.com/en-us/bingmaps/rest-services/imagery/imagery-metadata
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BingImageryMetadata{
   private String imageUrl;
   private String imageUrlSubdomains;
   private Integer imageWidth;
   private Integer imageHeight;
   private String vintageStart;
   private String vintageEnd;
   private Integer zoomMin;
   private Integer zoomMax;

   public BingImageryMetadata(){}
   public BingImageryMetadata(String imageUrl, String imageUrlSubdomains, Integer imageWidth, Integer imageHeight, String vintageStart, String vintageEnd, Integer zoomMin, Integer zoomMax )
   {
        this.imageUrl = imageUrl;
        this.imageUrlSubdomains = imageUrlSubdomains;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.vintageStart = vintageStart;
        this.vintageEnd = vintageEnd;
        this.zoomMin = zoomMin;
        this.zoomMax = zoomMax;
   }

    public String getImageUrl() {
        return this.imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrlSubdomains() {
        return this.imageUrlSubdomains;
    }

    public void setImageUrlSubdomains(String imageUrlSubdomains) {
        this.imageUrlSubdomains = imageUrlSubdomains;
    }

    public Integer getImageWidth() {
        return this.imageWidth;
    }

    public void setImageWidth(Integer imageWidth) {
        this.imageWidth = imageWidth;
    }

    public Integer getImageHeight() {
        return this.imageHeight;
    }

    public void setImageHeight(Integer imageHeight) {
        this.imageHeight = imageHeight;
    }

    public String getVintageStart() {
        return this.vintageStart;
    }

    public void setVintageStart(String vintageStart) {
        this.vintageStart = vintageStart;
    }

    public String getVintageEnd() {
        return this.vintageEnd;
    }

    public void setVintageEnd(String vintageEnd) {
        this.vintageEnd = vintageEnd;
    }

    public Integer getZoomMin() {
        return this.zoomMin;
    }

    public void setZoomMin(Integer zoomMin) {
        this.zoomMin = zoomMin;
    }

    public Integer getZoomMax() {
        return this.zoomMax;
    }

    public void setZoomMax(Integer zoomMax) {
        this.zoomMax = zoomMax;
    }
   

    @Override
    public String toString() {
        return "{" +
            " imageUrl='" + getImageUrl() + "'" +
            ", imageUrlSubdomains='" + getImageUrlSubdomains() + "'" +
            ", imageWidth='" + getImageWidth() + "'" +
            ", imageHeight='" + getImageHeight() + "'" +
            ", vintageStart='" + getVintageStart() + "'" +
            ", vintageEnd='" + getVintageEnd() + "'" +
            ", zoomMin='" + getZoomMin() + "'" +
            ", zoomMax='" + getZoomMax() + "'" +
            "}";
    }


}