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

/**
 * Java representation of:
 *
 * GenericSignage ::= SEQUENCE (SIZE(1..16)) OF SEQUENCE {
 *    item CHOICE {
 *        itis ITIScodes,
 *        text ITIStextPhrase
 *    }
 * }
 */
public class GenericSignage {

    private List<GenericSignageItem> items = new ArrayList<>();

    public GenericSignage() {
    }

    public GenericSignage(List<GenericSignageItem> items) {
        this.items = items;
    }

    public List<GenericSignageItem> getItems() {
        return items;
    }

    public void setItems(List<GenericSignageItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "GenericSignage{" +
                "items=" + items +
                '}';
    }

    // ---- Inner class ----
    public static class GenericSignageItem {

        public enum Choice {
            ITIS, TEXT
        }

        private Choice choice;
        private ITIScodes itis;
        private ITISTextPhrase text;

        public GenericSignageItem(ITIScodes itis) {
            this.choice = Choice.ITIS;
            this.itis = itis;
        }

        public GenericSignageItem(ITISTextPhrase text) {
            this.choice = Choice.TEXT;
            this.text = text;
        }

        public Choice getChoice() {
            return choice;
        }

        public ITIScodes getItis() {
            return itis;
        }

        public ITISTextPhrase getText() {
            return text;
        }

        @Override
        public String toString() {
            return "GenericSignageItem{" +
                    "choice=" + choice +
                    ", itis=" + itis +
                    ", text=" + text +
                    '}';
        }
    }
}
