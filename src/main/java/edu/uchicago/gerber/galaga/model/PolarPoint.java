package edu.uchicago.gerber.galaga.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PolarPoint {
    //we use the wrapper-class Double as members to get the Comparable interface
    //because Asteroid needs to sort by theta when generating random-shapes.
    private Double r; // corresponds to the hypotenuse in cartesean, number between 0 and 1
    private Double theta; //degrees in radians, number between 0 and 6.283
}
