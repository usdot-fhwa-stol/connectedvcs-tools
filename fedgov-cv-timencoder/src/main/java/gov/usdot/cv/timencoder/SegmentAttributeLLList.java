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

 package gov.usdot.cv.timencoder;

import java.util.ArrayList;
import java.util.List;

public class SegmentAttributeLLList {
    private List<SegmentAttributeLL> elements = new ArrayList<>();

    public SegmentAttributeLLList() {
    }

    public SegmentAttributeLLList(List<SegmentAttributeLL> elements) {
        this.elements = elements;
    }

    public List<SegmentAttributeLL> getElements() {
        return elements;
    }

    public int getElementSize() {
        return elements.size();
    }

    public void setElements(List<SegmentAttributeLL> elements) {
        this.elements = elements;
    }

    public void addElement(SegmentAttributeLL element) {
        if (elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(element);
    }

    @Override
    public String toString() {
        return "SegmentAttributeLLList{" +
                "elements=" + elements +
                '}';
    }
}
