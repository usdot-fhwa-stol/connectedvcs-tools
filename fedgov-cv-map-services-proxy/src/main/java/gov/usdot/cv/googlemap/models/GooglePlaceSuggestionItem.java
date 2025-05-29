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
package gov.usdot.cv.googlemap.models;

public class GooglePlaceSuggestionItem {
    private GooglePlacePrediction placePrediction;


    public GooglePlaceSuggestionItem() {
    }

    public GooglePlaceSuggestionItem(GooglePlacePrediction suggestions) {
        this.placePrediction = suggestions;
    }

    public GooglePlacePrediction getPlacePrediction() {
        return this.placePrediction;
    }

    public void setPlacePrediction(GooglePlacePrediction suggestions) {
        this.placePrediction = suggestions;
    }

    public GooglePlaceSuggestionItem suggestions(GooglePlacePrediction suggestions) {
        setPlacePrediction(suggestions);
        return this;
    }


    @Override
    public String toString() {
        return "{" +
            " suggestions='" + getPlacePrediction() + "'" +
            "}";
    }

}
