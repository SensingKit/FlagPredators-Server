package uk.ac.qmul.flagpredatorsserver.model;
/*
The MIT License (MIT)

Copyright (c) <2015> <Chris Veness>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

/**
 * Created by Ming-Jiun Huang on 15/7/20.
 * Contact me at m.huang@hss13.qmul.ac.uk
 *
 * Using half of boundary length to get the coordinates of four corners.
 * maxLat, minLat, maxLng and minLng can be obtained, seeing below.
 * 
 *                       (maxLat, centreLng)
 *                       —————————N—————————  
 *                      |                   |
 *                      |                   |
 * (centreLat, minLng)  W  RED    C   BLUE  E  (centreLat, maxLng)
 *                      |                   |
 *                      |                   |
 *                       —————————S—————————  
 *                       (minLat, centreLng)
 */
public class BoundingBox {
    // All latitudes (lat) and longitudes (lng) are in degree.
    private Double minLat;
    private Double minLng;
    private Double maxLat;
    private Double maxLng;
    private double centreLat;
    private double centreLng;
    private int boundaryDistance;
    private boolean isRed;
    private Double boxLat;
    private Double boxLng;

    double EARTH_RADIUS = 6378137; //The major equatorial radius in metres from WGS-84
    double f = 1 / 298.257223563; //The flattening from WGS-84
    double b = (EARTH_RADIUS * (1 - f)); //The polar semi-minor axis from WGS-84

    /** A constructor for square bounding boxes(playground).
    * Create a square bounding box by the given location of the centre point and boundary length.
    * centreLat - The latitude of the centre of the bounding box.
    * centreLng - The longitude of the centre of the bounding box.
    * boundaryLength - The length of each edge given by the settings from a game initiator.
    */
    public BoundingBox(double centreLat, double centreLng, int boundaryLength) {
        this.centreLat = centreLat;
        this.centreLng = centreLng;
        this.boundaryDistance = boundaryLength/2;
        double[] n = this.buildAccurateCoordinate(boundaryDistance, 0);
        double[] e = this.buildAccurateCoordinate(boundaryDistance, 90);
        double[] s = this.buildAccurateCoordinate(boundaryDistance, 180);
        double[] w = this.buildAccurateCoordinate(boundaryDistance, 270);
        this.minLat = s[0];
        this.minLng = w[1];
        this.maxLat = n[0];
        this.maxLng = e[1];
        System.out.println(toString());
    }

    /** A constructor for round bounding boxes(flags or bases).
    * Create a round bounding box by the given location of the centre point and boundary length.
    * centreLat - The latitude of the centre point of the playground.
    * centreLng - The longitude of the centre point of the playground.
    * isRed - A flag for defining this round bounding box is for red team(true) or blue team(false).
    * boundaryLength - The length of each edge given by the settings from a game initiator.
    */
    public BoundingBox(double centreLat, double centreLng, boolean isRed, int boundaryLength){    //** 
        this.centreLat = centreLat;
        this.centreLng = centreLng;
        this.isRed = isRed;
        this.boundaryDistance = boundaryLength/2;
        this.setBoxLocation();
        this.minLat = null;
        this.minLng = null;
        this.maxLat = null;
        this.maxLng = null;
        System.out.println(toString());
    }

    /**
     * Place a bounding box(flag or base) in a proper location. 
     * Red team is on the north side of the playground, whereas blue team is on the south side.
     * The distance between the box and centre point of playground is 70 percent of half of
     * the boundary length. The longitudes of flags, bases and centre points are the same.
     */
    public void setBoxLocation(){
        double objectDistance = (boundaryDistance * 7 / 10);
        if(this.isRed){
            boxLat = this.buildCoordinate(objectDistance, 0)[0];
            boxLng = this.buildCoordinate(objectDistance, 0)[1];
        }else{
            boxLat = this.buildCoordinate(objectDistance, 180)[0];
            boxLng = this.buildCoordinate(objectDistance, 180)[1];
        }
    }

/**
 * Return a destination location (Latitude and Longitude) in a double[] array by the given
 * see http://williams.best.vwh.net/avform.htm#LL
 * distance - Distance away from the centre point in metres.
 * bearing - The bearing is a clockwise from north in degree [θ].
 * Return the cooridnate of the destination point in degree. [0]Latitude, [1]Longitude
 */
    public double[] buildCoordinate(double distance, double bearing){
        //The results are calculated in radian.
        double cLat = this.toRadians(centreLat); 
        double cLng = this.toRadians(centreLng); 
        double angularDistance = distance/EARTH_RADIUS; //The angular distance in radians.
        double θ = this.toRadians(bearing); //The bearing in radians [θ].
        double[] dest = new double[2];
        //dest[0]: The latitude of destination.
        dest[0] = Math.asin(Math.sin(cLat) * Math.cos(angularDistance) +
                Math.cos(cLat) * Math.sin(angularDistance) * Math.cos(θ)); 
        //dest[1]: The longitude of destination.
        dest[1] = cLng + Math.atan2(Math.sin(θ) * Math.sin(angularDistance) * Math.cos(cLat),
                Math.cos(angularDistance) - Math.sin(cLat) * Math.sin(dest[0])); 
        //Normalise to -180 ~ +180°
        dest[1] = (dest[1] + 3 * Math.PI) % (2 * Math.PI) - Math.PI; 
        //Convert values back to degree.
        dest[0] = this.toDegree(dest[0]); 
        dest[1] = this.toDegree(dest[1]);
        return dest;
    }

 /**
 * This method is an implementation of Vincenty solutions of geodesics based on code wirtten
 * by Chris Veness. 
 */
    public double[] buildAccurateCoordinate(double distance, double bearing){
        double cLat = this.toRadians(centreLat); 
        double cLng = this.toRadians(centreLng); 
        double angularDistance = distance/EARTH_RADIUS; //The angular distance in radians.
        double α1 = this.toRadians(bearing);
        double s = distance;

        double sinα1 = Math.sin(α1);
        double cosα1 = Math.cos(α1);
        double tanU1 = (1-f) * Math.tan(cLat);
        double cosU1 = 1 / Math.sqrt((1 + tanU1*tanU1));
        double sinU1 = tanU1 * cosU1;
        double σ1 = Math.atan2(tanU1, cosα1);
        double sinα = cosU1 * sinα1;
        double cosSqα = 1 - sinα * sinα;

        double uSq = cosSqα * (EARTH_RADIUS * EARTH_RADIUS - b * b) / (b * b);
        double A = 1 + uSq/16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq/1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        
        double cos2σM, sinσ, cosσ, Δσ;
        double σ = s / (b * A);
        double σʹ;
        double iterations = 0;
        do {
            cos2σM = Math.cos(2*σ1 + σ);
            sinσ = Math.sin(σ);
            cosσ = Math.cos(σ);
            Δσ = B * sinσ * (cos2σM + B/4 * (cosσ*(-1 + 2*cos2σM*cos2σM) -
                B/6 * cos2σM * (-3 + 4 * sinσ * sinσ) * (-3 + 4*cos2σM*cos2σM)));
            σʹ = σ;
            σ = s / (b * A) + Δσ;
        } while (Math.abs(σ-σʹ) > 1e-12 && ++iterations < 200);
        if (iterations>=200){
            System.out.println("Formula failed to converge"); 
        } 

        double[] dest = new double[2];
        double x = sinU1 * sinσ - cosU1 * cosσ * cosα1;
        dest[0] = Math.atan2(sinU1 * cosσ + cosU1 * sinσ * cosα1, (1-f)* Math.sqrt(sinα * sinα + x * x));
        double λ = Math.atan2(sinσ * sinα1, cosU1 * cosσ - sinU1 * sinσ * cosα1);
        double C = f/16 * cosSqα * (4 + f * (4 - 3 * cosSqα));
        double L = λ - (1-C) * f * sinα *
                (σ + C * sinσ * (cos2σM + C * cosσ * (-1 + 2 * cos2σM * cos2σM)));
        //Normalise to -180...+180
        dest[1] = (cLng + L + 3 * Math.PI) % (2 * Math.PI) - Math.PI;  
        //bearing
        double α2 = Math.atan2(sinα, - x);
        //Normalise to 0 ~ 360°
        α2 = (α2 + 2*Math.PI) % (2*Math.PI); 
        //Convert values back to degree.
        dest[0] = this.toDegree(dest[0]); 
        dest[1] = this.toDegree(dest[1]);
        return dest;
    }

/**
 * Calculate the distance between two points.
 * This method is an implementation of Haversine fomula based on code wirtten by Chris Veness
 * [Available from 2015.08.01] http://www.movable-type.co.uk/scripts/latlong.html
 * destLat - The latitude of the destination point in degree. 
 * destLng - The longitude of the detination point in degree. 
 * Return the shortest distance between two points.
 */
    public double getDistance(double destLat, double destLng){
        double sLat = this.toRadians(boxLat);
        double dLat = this.toRadians(destLat);
        double sLng = this.toRadians(boxLng);
        double dLng = this.toRadians(destLng);
        double latDifference = this.toRadians(dLat - sLat);
        double lngDifference = this.toRadians(dLng - sLng);

        double a = Math.sin(latDifference/2) * Math.sin(latDifference/2) +
                Math.cos(sLat) * Math.cos(dLat) *
                Math.sin(lngDifference/2) * Math.sin(lngDifference/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double distance = EARTH_RADIUS * c;
        return distance;
    }

/**
 * Calculate the distance between two points.
 * This method is an implementation of Vincenty solutions of geodesics based on code wirtten
 * by Chris Veness. 
 * [Available from 2015.08.07] http://www.movable-type.co.uk/scripts/latlong-vincenty.html
 * destLat - The latitude of the destination point in degree. 
 * destLng - The longitude of the detination point in degree.
 * Return the shortest distance between two points.  
 */
    public double getAccurateDistance(double destLat, double destLng){
        double φ1 = this.toRadians(boxLat);
        double φ2 = this.toRadians(destLat);
        double λ1 = this.toRadians(boxLng);
        double λ2 = this.toRadians(destLng);
        double L = λ2 - λ1; //The difference in Longitude
        double tanU1 = (1-f) * Math.tan(φ1);
        double cosU1 = 1 / Math.sqrt((1 + tanU1 * tanU1));
        double sinU1 = tanU1 * cosU1;
        double tanU2 = (1-f) * Math.tan(φ2);
        double cosU2 = 1 / Math.sqrt((1 + tanU2 * tanU2));
        double sinU2 = tanU2 * cosU2;
        double λ = L;
        double λʹ, iterationLimit = 100;
        double sinλ, cosλ, sinSqσ, sinσ, cosσ, σ, sinα, cosSqα, cos2σM, C;
        do {
            sinλ = Math.sin(λ);
            cosλ = Math.cos(λ);
            sinSqσ = (cosU2 * sinλ) * (cosU2 * sinλ) + 
                            (cosU1 * sinU2 - sinU1 * cosU2 * cosλ) * (cosU1 * sinU2 - sinU1 * cosU2 * cosλ);
            sinσ = Math.sqrt(sinSqσ);
            if (sinσ==0){ return 0; }   // Co-incident points
            cosσ = (sinU1 * sinU2) + (cosU1 * cosU2 * cosλ);
            σ = Math.atan2(sinσ, cosσ);
            sinα = cosU1 * cosU2 * sinλ / sinσ;
            cosSqα = 1 - (sinα * sinα);
            cos2σM = cosσ - (2 * sinU1 * sinU2 / cosSqα);
            if (Double.isNaN(cos2σM)){ cos2σM = 0; }   // Equatorial line: cosSqα=0 (§6)
            C = f / 16 * cosSqα * (4 + f * (4 - 3 * cosSqα));
            λʹ = λ;
            λ = L + (1 - C) * f * sinα * (σ + C * sinσ * (cos2σM + C * cosσ * (-1 + 2 * cos2σM * cos2σM)));
        } while (Math.abs(λ-λʹ) > 1e-12 && --iterationLimit > 0);
        if (iterationLimit == 0) {
            System.out.println("Formula failed to converge!");
            //return null; //Error
        }

        double uSq = cosSqα * (EARTH_RADIUS * EARTH_RADIUS - b * b) / (b * b);
        double A = 1 + uSq/16384 * (4096 + uSq * (-768 + uSq * (320 - 175 * uSq)));
        double B = uSq/1024 * (256 + uSq * (-128 + uSq * (74 - 47 * uSq)));
        double Δσ = B * sinσ * (cos2σM + B / 4 * (cosσ * (-1 + 2 * cos2σM * cos2σM) -
                    B / 6 * cos2σM * (-3 + 4 * sinσ * sinσ) * (-3 + 4 * cos2σM * cos2σM)));

        double distance = b * A * (σ - Δσ);
        //bearing
        double fwdAz = Math.atan2((cosU2 * sinλ), (cosU1 * sinU2) - (sinU1 * cosU2 * cosλ));
        double revAz = Math.atan2((cosU1 * sinλ), (-sinU1 * cosU2) + (cosU1 * sinU2 * cosλ));

        return distance;
    }

//Convert the unit from degree to radians.
    public double toRadians(double degree){
         return (degree * Math.PI / 180);
    }
//Convert the unit from radians to degree.
    public double toDegree(double radians){
        return (radians * 180 / Math.PI);
    }

//Getters: coordinates in degree.
    public double[] getBoxLocation(){
        double[] boxLoaction = { boxLat, boxLng};
        return boxLoaction;
    }
    public double getMinLat(){
        return minLat;
    }
    public double getMinLng(){
        return minLng;
    }
    public double getMaxLat(){
        return maxLat;
    }
    public double getMaxLng(){
        return maxLng;
    }

/**
 *Check whether the given location is in the bounds of this square bounding box.
 *lat - the latitude in degree
 *lng - the longitude in degree.
 */
    public boolean checkInBoundsByCoordinate(double lat, double lng){
        //Latitude in degree
        if(minLat < maxLat){
            if(minLat <= lat && lat <= maxLat){
                //Longitude in degree
                if(minLng < maxLng && minLng <= lng && lng <= maxLng){
                    return true;
                //if the bounding box contains antimeridian
                }else if (minLng > maxLng && (minLng <= lng || lng <= maxLng)){ 
                    return true;
                }
            }
        }else{
            //The poles
        }
        return false;
    }

    public String toString(){
        String msg = "BOUNDING BOX INFO<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<\n";
        if(maxLat != null){
            msg += "[Centre] Latitude: " + centreLat + " Longitude: " + centreLng + "\n" + 
                    "Boundary Distance: " + boundaryDistance + "\n" +
                    "Max Latitude: " + maxLat + " Max Longitude: " + maxLng + "\n" +
                    "Min Latitude: " + minLat + " Min Longitude: " + minLng;
        }else{
            msg += "[Centre] Latitude: " + centreLat + " Longitude: " + centreLng + "\n" + 
                    "Boundary Distance: " + boundaryDistance + "\n" +
                    "Box Latitude: " + boxLat + " Box Longitude: " + boxLng;
        }
        return msg;
    }

//**Testing
    public static void main(String[] args){
        BoundingBox b = new BoundingBox(51.4778, -0.0015, 100);
        System.out.println("Degree: [0]" + b.centreLat + " [1]" + b.centreLng);
        System.out.println("Radians: [0]" + b.toRadians(b.centreLat) + " [1]" + b.toRadians(b.centreLng));

        System.out.println("Lat: " + b.toRadians(51.5265182) + " Lng: " + b.toRadians(-0.0407556));

        double[] ne = b.buildCoordinate(50.0, 45);
        System.out.println("NE: <" + ne[0] + "> <" + ne[1] + "> NE[toRadians]: [" + b.toRadians(ne[0]) + "] [" + b.toRadians(ne[1]) + "]");
        double[] se = b.buildCoordinate(50.0, 135);
        System.out.println("SE: <" + se[0] + "> <" + se[1] + "> SE[toRadians]: [" + b.toRadians(se[0]) + "] [" + b.toRadians(se[1]) + "]");
        double[] sw = b.buildCoordinate(50.0, 225);
        System.out.println("SW: <" + sw[0] + "> <" + sw[1] + "> W[toRadians]: [" + b.toRadians(sw[0]) + "] [" + b.toRadians(sw[1]) + "]");
        double[] nw = b.buildCoordinate(50.0, 315);
        System.out.println("NW: <" + nw[0] + "> <" + nw[1] + "> NW[toRadians]: [" + b.toRadians(nw[0]) + "] [" + b.toRadians(nw[1]) + "]");

        System.out.println("=======================================================================");

        BoundingBox here = new BoundingBox(51.5265298, -0.0407712, 100);
        double[] location = {51.5264817, -0.0407396};
        System.out.println("SW: " + here.getMinLat() + ", " + here.getMinLng());
        System.out.println("NE: " + here.getMaxLat() + ", " + here.getMaxLng());
        if(here.checkInBoundsByCoordinate(location[0], location[1])){
            System.out.println("yes");
        }
        System.out.println("=======================================================================");
        BoundingBox aDirect = new BoundingBox(51.5265298, -0.0407712, 50);
        BoundingBox red = new BoundingBox(51.5265298, -0.0407712, true, 50);
        BoundingBox blue = new BoundingBox(51.5265298, -0.0407712, false, 50);
    }
}
