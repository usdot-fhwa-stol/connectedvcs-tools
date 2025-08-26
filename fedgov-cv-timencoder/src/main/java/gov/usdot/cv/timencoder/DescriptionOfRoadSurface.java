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

public class DescriptionOfRoadSurface {

    public enum Choice {
        PORTLAND_CEMENT,
        ASPHALT_OR_TAR,
        GRAVEL,
        GRASS,
        CINDERS,
        ROCK,
        ICE,
        SNOW
    }

    private Choice choice;

    private PortlandCement portlandCement;
    private AsphaltOrTar asphaltOrTar;
    private Gravel gravel;
    private Grass grass;
    private Cinders cinders;
    private Rock rock;
    private Ice ice;
    private Snow snow;

    public DescriptionOfRoadSurface() {
    }

    // Convenience constructors for each alternative
    public DescriptionOfRoadSurface(PortlandCement value) {
        setPortlandCement(value);
    }

    public DescriptionOfRoadSurface(AsphaltOrTar value) {
        setAsphaltOrTar(value);
    }

    public DescriptionOfRoadSurface(Gravel value) {
        setGravel(value);
    }

    public DescriptionOfRoadSurface(Grass value) {
        setGrass(value);
    }

    public DescriptionOfRoadSurface(Cinders value) {
        setCinders(value);
    }

    public DescriptionOfRoadSurface(Rock value) {
        setRock(value);
    }

    public DescriptionOfRoadSurface(Ice value) {
        setIce(value);
    }

    public DescriptionOfRoadSurface(Snow value) {
        setSnow(value);
    }

    public Choice getChoice() {
        return choice;
    }

    public PortlandCement getPortlandCement() {
        return portlandCement;
    }

    public void setPortlandCement(PortlandCement v) {
        clearAll();
        this.choice = Choice.PORTLAND_CEMENT;
        this.portlandCement = v;
    }

    public AsphaltOrTar getAsphaltOrTar() {
        return asphaltOrTar;
    }

    public void setAsphaltOrTar(AsphaltOrTar v) {
        clearAll();
        this.choice = Choice.ASPHALT_OR_TAR;
        this.asphaltOrTar = v;
    }

    public Gravel getGravel() {
        return gravel;
    }

    public void setGravel(Gravel v) {
        clearAll();
        this.choice = Choice.GRAVEL;
        this.gravel = v;
    }

    public Grass getGrass() {
        return grass;
    }

    public void setGrass(Grass v) {
        clearAll();
        this.choice = Choice.GRASS;
        this.grass = v;
    }

    public Cinders getCinders() {
        return cinders;
    }

    public void setCinders(Cinders v) {
        clearAll();
        this.choice = Choice.CINDERS;
        this.cinders = v;
    }

    public Rock getRock() {
        return rock;
    }

    public void setRock(Rock v) {
        clearAll();
        this.choice = Choice.ROCK;
        this.rock = v;
    }

    public Ice getIce() {
        return ice;
    }

    public void setIce(Ice v) {
        clearAll();
        this.choice = Choice.ICE;
        this.ice = v;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow v) {
        clearAll();
        this.choice = Choice.SNOW;
        this.snow = v;
    }

    private void clearAll() {
        this.portlandCement = null;
        this.asphaltOrTar = null;
        this.gravel = null;
        this.grass = null;
        this.cinders = null;
        this.rock = null;
        this.ice = null;
        this.snow = null;
    }

    @Override
    public String toString() {
        return "DescriptionOfRoadSurface{" +
                "choice=" + choice +
                ", portlandCement=" + portlandCement +
                ", asphaltOrTar=" + asphaltOrTar +
                ", gravel=" + gravel +
                ", grass=" + grass +
                ", cinders=" + cinders +
                ", rock=" + rock +
                ", ice=" + ice +
                ", snow=" + snow +
                '}';
    }
}
