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

public class NodeOffsetPointLL {
    private int choice;
    private int node_LL1_chosen;
    private int node_LL2_chosen;
    private int node_LL3_chosen;
    private int node_LL4_chosen;
    private int node_LL5_chosen;
    private int node_LL6_chosen;
    private int node_LatLon_chosen;
    private int regional_chosen;

    // Choices
    public static final int NODE_LL1 = 1;
    public static final int NODE_LL2 = 2;
    public static final int NODE_LL3 = 3;
    public static final int NODE_LL4 = 4;
    public static final int NODE_LL5 = 5;
    public static final int NODE_LL6 = 6;
    public static final int NODE_LATLON = 7;
    public static final int REGIONAL = 8;

    public NodeOffsetPointLL() {

    }

    public NodeOffsetPointLL(int choice, int node_LL1_chosen, int node_LL2_chosen, int node_LL3_chosen,
            int node_LL4_chosen, int node_LL5_chosen, int node_LL6_chosen, int node_LatLon_chosen,
            int regional_chosen) {
        this.choice = choice;
        this.node_LL1_chosen = node_LL1_chosen;
        this.node_LL2_chosen = node_LL2_chosen;
        this.node_LL3_chosen = node_LL3_chosen;
        this.node_LL4_chosen = node_LL4_chosen;
        this.node_LL5_chosen = node_LL5_chosen;
        this.node_LL6_chosen = node_LL6_chosen;
        this.node_LatLon_chosen = node_LatLon_chosen;
        this.regional_chosen = regional_chosen;
    }

    public int getChoice() {
        return choice;
    }
    public void setChoice(int choice) {
        this.choice = choice;
    }
    public int getNode_LL1_chosen() {
        return node_LL1_chosen;
    }
    public void setNode_LL1_chosen(int node_LL1_chosen) {
        this.node_LL1_chosen = node_LL1_chosen;
    }
    public int getNode_LL2_chosen() {
        return node_LL2_chosen;
    }
    public void setNode_LL2_chosen(int node_LL2_chosen) {
        this.node_LL2_chosen = node_LL2_chosen;
    }
    public int getNode_LL3_chosen() {
        return node_LL3_chosen;
    }
    public void setNode_LL3_chosen(int node_LL3_chosen) {
        this.node_LL3_chosen = node_LL3_chosen;
    }
    public int getNode_LL4_chosen() {
        return node_LL4_chosen;
    }
    public void setNode_LL4_chosen(int node_LL4_chosen) {
        this.node_LL4_chosen = node_LL4_chosen;
    }
    public int getNode_LL5_chosen() {
        return node_LL5_chosen;
    }
    public void setNode_LL5_chosen(int node_LL5_chosen) {
        this.node_LL5_chosen = node_LL5_chosen;
    }
    public int getNode_LL6_chosen() {
        return node_LL6_chosen;
    }
    public void setNode_LL6_chosen(int node_LL6_chosen) {
        this.node_LL6_chosen = node_LL6_chosen;
    }
    public int getNode_LatLon_chosen() {
        return node_LatLon_chosen;
    }
    public void setNode_LatLon_chosen(int node_LatLon_chosen) {
        this.node_LatLon_chosen = node_LatLon_chosen;
    }
    public int getRegional_chosen() {
        return regional_chosen;
    }
    public void setRegional_chosen(int regional_chosen) {
        this.regional_chosen = regional_chosen;
    }

    @Override
    public String toString() {
        return "NodeOffsetPointLL{" +
                "choice=" + choice
                + ", node_LL1_chosen=" + node_LL1_chosen
                + ", node_LL2_chosen=" + node_LL2_chosen
                + ", node_LL3_chosen=" + node_LL3_chosen
                + ", node_LL4_chosen=" + node_LL4_chosen
                + ", node_LL5_chosen=" + node_LL5_chosen
                + ", node_LL6_chosen=" + node_LL6_chosen
                + ", node_LatLon_chosen=" + node_LatLon_chosen
                + ", regional_chosen=" + regional_chosen
                + '}';
    }
    
}
