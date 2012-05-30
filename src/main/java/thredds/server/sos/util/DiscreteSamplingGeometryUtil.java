package thredds.server.sos.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.Structure;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.CF;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.ProfileFeature;
import ucar.nc2.ft.ProfileFeatureCollection;
import ucar.nc2.ft.StationProfileFeature;
import ucar.nc2.ft.StationProfileFeatureCollection;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.ft.TrajectoryFeature;
import ucar.nc2.ft.TrajectoryFeatureCollection;
import ucar.nc2.ft.grid.Grid;
import ucar.nc2.ft.point.StationPointFeature;
import ucar.nc2.ft.point.standard.StandardProfileCollectionImpl;

/**
 *
 * @author tkunicki
 * @modified Andy Bird 
 */
public class DiscreteSamplingGeometryUtil {

    @Deprecated
    public static StationTimeSeriesFeatureCollection extractStationTimeSeriesFeatureCollection(FeatureDataset featureDataset) throws IOException {
        if (featureDataset instanceof FeatureDatasetPoint) {
            FeatureDatasetPoint featureDatasetPoint = (FeatureDatasetPoint) featureDataset;
            List<FeatureCollection> featureCollectionList = featureDatasetPoint.getPointFeatureCollectionList();
            if (featureCollectionList != null && featureCollectionList.size() > 0) {
                if (featureCollectionList.size() == 1) {
                    FeatureCollection featureCollection = featureCollectionList.get(0);

                    if (featureCollection instanceof StationTimeSeriesFeatureCollection) {
                        return (StationTimeSeriesFeatureCollection) featureCollection;
                    } //**********         
                    //used to highlight feature type        
                    else if (featureCollection instanceof StationProfileFeatureCollection) {
                        //System.out.println("profile feature collection");
                    } else if (featureCollection instanceof StationPointFeature) {
                        //System.out.println("point feature");
                    } else if (featureCollection instanceof StationProfileFeature) {
                        //System.out.println("profile feature");
                    } else if (featureCollection instanceof StationTimeSeriesFeature) {
                        //System.out.println("StationTimeSeriesFeature feature");
                    } else if (featureCollection instanceof ProfileFeature) {
                        //System.out.println("profile feature");
                    } else if (featureCollection instanceof ProfileFeatureCollection) {
                        //System.out.println("profile feature collection");
                    } else if (featureCollection instanceof Grid) {
                        //System.out.println("profile feature collection");
                    } else if (featureCollection instanceof TrajectoryFeature) {
                        //System.out.println("profile feature collection");
                    }
                    //********** 


                } else {
                    // multiple collections???
                }
            } else {
                // error, no data
            }
        } else {
            // error, no data or wrong data type
        }
        return null;
    }

    public static List<VariableSimpleIF> getDataVariables(FeatureDataset dataset) throws IOException {

        List<VariableSimpleIF> variableList = null;

        if (dataset == null) {
            System.out.println("ERROR:Null Data");
        }

        //System.out.println(dataset.getFeatureType());
        switch (dataset.getFeatureType()) {
            case POINT:
            case PROFILE:
            case SECTION:
            case STATION:
            case STATION_PROFILE:
            case STATION_RADIAL:
            case TRAJECTORY:

                variableList = new ArrayList<VariableSimpleIF>();

                // Try Unidata Observation Dataset convention where observation
                // dimension is declared as global attribute...
                Attribute convAtt = dataset.getNetcdfFile().findGlobalAttributeIgnoreCase("Conventions");
                if (convAtt != null && convAtt.isString()) {
                    String convName = convAtt.getStringValue();

                    //// Unidata Observation Dataset Convention
                    //   http://www.unidata.ucar.edu/software/netcdf-java/formats/UnidataObsConvention.html
                    if (convName.contains("Unidata Observation Dataset")) {
                        Attribute obsDimAtt = dataset.findGlobalAttributeIgnoreCase("observationDimension");
                        String obsDimName = (obsDimAtt != null && obsDimAtt.isString())
                                ? obsDimAtt.getStringValue() : null;
                        if (obsDimName != null && obsDimName.length() > 0) {
                            String psuedoRecordPrefix = obsDimName + '.';
                            for (VariableSimpleIF var : dataset.getNetcdfFile().getVariables()) {
                                if (var.findAttributeIgnoreCase("_CoordinateAxisType") == null) {
                                    if (var.getName().startsWith(psuedoRecordPrefix)) {
                                        // doesn't appear to be documented, this
                                        // is observed behavior...
                                        variableList.add(var);
                                    } else {
                                        for (Dimension dim : var.getDimensions()) {
                                            if (obsDimName.equalsIgnoreCase(dim.getName())) {
                                                variableList.add(var);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (variableList.isEmpty()) {
                            // no explicit observation dimension found? look for
                            // variables with unlimited dimension
                            for (VariableSimpleIF var : dataset.getNetcdfFile().getVariables()) {
                                for (Dimension dim : var.getDimensions()) {
                                    if (dim.isUnlimited()) {
                                        variableList.add(var);
                                    }
                                }
                            }
                        }
                    }
                }

                //// CF Conventions
                //   https://cf-pcmdi.llnl.gov/trac/wiki/PointObservationConventions
                //
                //  Don't try explicit :Conventions attribute check since this
                //  doesnt seem to be coming through TDS with cdmremote when
                //  CF conventions are used (?!)
                if (variableList.isEmpty()) {
                    // Try CF convention where range variable has coordinate attribute
                    for (Variable variable : dataset.getNetcdfFile().getVariables()) {
                        if (variable.findAttributeIgnoreCase("coordinates") != null) {
                            if (variable instanceof Structure) {
                                for (Variable structureVariable : ((Structure) variable).getVariables()) {
                                    if (!(structureVariable instanceof CoordinateAxis)
                                            && structureVariable.findAttributeIgnoreCase(CF.RAGGED_PARENTINDEX) == null) {
                                        variableList.add(structureVariable);
                                    }
                                }
                            } else {
                                variableList.add(variable);
                            }
                        }
                    }
                }
                break;
            default:
                for (VariableSimpleIF var : dataset.getDataVariables()) {
                    variableList.add(var);
                }
                break;
        }

        if (variableList == null) {
            variableList = Collections.emptyList();
        }
        return variableList;
    }

    @Deprecated
    public static StationProfileFeatureCollection extractStationProfileFeatureCollection(FeatureDataset featureDataset) {
        if (featureDataset instanceof FeatureDatasetPoint) {
            FeatureDatasetPoint featureDatasetPoint = (FeatureDatasetPoint) featureDataset;
            List<FeatureCollection> featureCollectionList = featureDatasetPoint.getPointFeatureCollectionList();
            if (featureCollectionList != null && featureCollectionList.size() > 0) {
                if (featureCollectionList.size() == 1) {
                    FeatureCollection featureCollection = featureCollectionList.get(0);

                    if (featureCollection instanceof StationProfileFeatureCollection) {
                        return (StationProfileFeatureCollection) featureCollection;
                    }

                } else {
                    // multiple collections???
                }
            } else {
                // error, no data
            }
        } else {
            // error, no data or wrong data type
        }
        return null;
    }

    @Deprecated
    public static ProfileFeatureCollection extractStdProfileCollection(FeatureDataset featureDataset) {
        if (featureDataset instanceof FeatureDatasetPoint) {
            FeatureDatasetPoint featureDatasetPoint = (FeatureDatasetPoint) featureDataset;
            List<FeatureCollection> featureCollectionList = featureDatasetPoint.getPointFeatureCollectionList();
            if (featureCollectionList != null && featureCollectionList.size() > 0) {
                if (featureCollectionList.size() == 1) {
                    FeatureCollection featureCollection = featureCollectionList.get(0);

                    if (featureCollection instanceof ProfileFeatureCollection) {
                        return (ProfileFeatureCollection) featureCollection;
                    }

                } else {
                    // multiple collections???
                }
            } else {
                // error, no data
            }
        } else {
            // error, no data or wrong data type
        }
        return null;
    }

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
                        //System.out.println("point feature");
                    } else if (featureCollection instanceof StationProfileFeature) {
                        //System.out.println("profile feature");
                    } else if (featureCollection instanceof StationTimeSeriesFeature) {
                        //System.out.println("StationTimeSeriesFeature feature");
                    } else if (featureCollection instanceof ProfileFeature) {
                        //System.out.println("profile feature");
                    } else if (featureCollection instanceof ProfileFeatureCollection) {
                        return (ProfileFeatureCollection) featureCollection;
                    } else if (featureCollection instanceof TrajectoryFeature) {
                        System.out.println("Trajectory");
                        return (TrajectoryFeature) featureCollection;
                    } else if (featureCollection instanceof TrajectoryFeatureCollection) {
                        System.out.println("Trajectory Collection");
                        return (TrajectoryFeatureCollection) featureCollection;
                    }
                    //********** 


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
