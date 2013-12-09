package com.asascience.ncsos.util;

import ucar.nc2.dt.GridDataset;
import ucar.nc2.ft.*;
import ucar.nc2.ft.point.StationPointFeature;

import java.util.List;

public class DiscreteSamplingGeometryUtil {

    public static FeatureCollection extractFeatureDatasetCollection(FeatureDataset featureDataset) {
        if (featureDataset instanceof FeatureDatasetPoint) {
            FeatureDatasetPoint featureDatasetPoint = (FeatureDatasetPoint) featureDataset;
            List<FeatureCollection> featureCollectionList = featureDatasetPoint.getPointFeatureCollectionList();
            if (featureCollectionList != null && featureCollectionList.size() > 0) {
                if (featureCollectionList.size() == 1) {
                    FeatureCollection featureCollection = featureCollectionList.get(0);

                    if (featureCollection instanceof StationTimeSeriesFeatureCollection) {
                        return (StationTimeSeriesFeatureCollection) featureCollection;
                        
                    } else if (featureCollection instanceof StationProfileFeatureCollection) {
                        return (StationProfileFeatureCollection) featureCollection;
                        
                    } else if (featureCollection instanceof StationPointFeature) {
                        return (PointFeatureCollection) featureCollection;
                        //System.out.println("point feature");
                        
                    } else if (featureCollection instanceof StationProfileFeature) {
                        return (StationProfileFeatureCollection) featureCollection;
                        //System.out.println("profile feature");
                        
                    } else if (featureCollection instanceof StationTimeSeriesFeature) {
                        return (StationTimeSeriesFeatureCollection) featureCollection;
                        //System.out.println("StationTimeSeriesFeature feature");
                        
                    } else if (featureCollection instanceof ProfileFeature) {
                        return (ProfileFeatureCollection) featureCollection;
                        //System.out.println("profile feature");
                        
                    } else if (featureCollection instanceof ProfileFeatureCollection) {
                        return (ProfileFeatureCollection) featureCollection;
                        
                    } else if (featureCollection instanceof TrajectoryFeature) {
                        return (TrajectoryFeature) featureCollection;
                        
                    } else if (featureCollection instanceof TrajectoryFeatureCollection) {
                        return (TrajectoryFeatureCollection) featureCollection;
                        
                    } else if (featureCollection instanceof PointFeatureCollection) {
                        return (PointFeatureCollection) featureCollection;
                        
                    } else if (featureCollection instanceof SectionFeature) {
                        return (SectionFeature) featureCollection;
                        
                    } else if (featureCollection instanceof SectionFeatureCollection) {
                        return (SectionFeatureCollection) featureCollection;
                        
                    }
                    else {
                        System.err.println("Unable to find the feature dataset collection of " + featureCollection.getName());
                    }


                } else {
                    // multiple collections???
                }
            } else {
                // error, no data
            }
        } else {
            //GRIDDED DATASET             
            // error, no data or wrong data type

        }
        return null;
    }

    public static GridDataset extractGridDatasetCollection(FeatureDataset featureDataset) {
        if (featureDataset instanceof GridDataset) {
            return (GridDataset) featureDataset;
        }
        return null;
    }
}
