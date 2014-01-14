# NcSOS

[![Build Status](https://travis-ci.org/asascience-open/ncSOS.png?branch=master)](https://travis-ci.org/asascience-open/ncSOS)

Stable version: **RC8**

NcSOS adds an OGC SOS service to datasets in your existing [THREDDS](http://www.unidata.ucar.edu/projects/THREDDS/) server.  It complies with the [IOOS SWE Milestone 1.0](https://code.google.com/p/ioostech/source/browse/#svn%2Ftrunk%2Ftemplates%2FMilestone1.0) templates and requires your datasets be in any of the [CF 1.6 Discrete Sampling Geometries](http://cf-pcmdi.llnl.gov/documents/cf-conventions/1.6/cf-conventions.html#discrete-sampling-geometries).

NcSOS acts like other THREDDS services (such an OPeNDAP and WMS) where as there are individual service endpoints for each dataset.  It is best to aggregate your files and enable the NcSOS service on top of the aggregation.  i.e. The NcML aggregate of hourly files from an individual station would be a good candidate to serve with NcSOS.  Serving the individual hourly files with NcSOS would not be as beneficial.

_You will need a working THREDDS installation of a least version **4.3.16** to run NcSOS_.

## Installation
1. [Download](https://github.com/asascience-open/ncSOS/raw/master/jar/ncSOS.zip) the latest release of ncSOS
2. Extract `ncSOS.zip` into a local directory
3. Copy the `ncSOS.jar` into your `$TOMCAT_HOME/webapps/thredds/WEB-INF/lib` directory.
4. Copy the `sos-servlet.xml` configuration file into the `$TOMCAT_HOME/webapps/thredds/WEB-INF` directory.
5. Add two new servlet mappings to your `$TOMCAT_HOME/webapps/thredds/WEB-INF/web.xml` file:
    ```xml
    <servlet>
        <servlet-name>sos</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <load-on-startup>1</load-on-startup>
    </servlet>
    ```
    
    ```xml
    <servlet-mapping>
        <servlet-name>sos</servlet-name>
        <url-pattern>/sos/*</url-pattern>
    </servlet-mapping>
    ```

6. Add the following service definition to enable SOS in your THREDDS catalog XML files:
```xml
<service name="sos" serviceType="SOS" base="/thredds/sos/" />
``` 
7. Restart Tomcat

## NC Aggregation 

see the known issues section for a note on NCML aggregation...

Aggregated netcdf files can be served through the catalog by using NCML. NcML is an XML representation of netCDF metadata, more information on NCML can be found at the unidata website [here](http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/ncml/). In short NCML aggregation offers a seemless view and data access across multiple netcdf files.

Unidata also offers a Basic NCML tutorial [here](http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/ncml/v2.2/Tutorial.html), with a tutorial geared towards NCML aggregation [here](http://www.unidata.ucar.edu/software/thredds/current/netcdf-java/ncml/v2.2/Aggregation.html). There are a number of different methods for agreegating Netcdf data through NCML, it is suggest that you read the above tutorials, particually the NCML agreegation one, and decide the best method for aggregating your data.


A simple example aggregation, using a simple union can be seen below. The example implemention outlines some of the steps you should take.

1) identify how you are going to aggregate your netcdf data (in this example taken from unidate's NCML aggregation page we are using an aggregation on an existing dimension named "time", this might be a typical approach for the aggregation of time series files.


2) generate an NCML file using a text editior, something like this...

 ```xml
<netcdf xmlns="http://www.unidata.ucar.edu/namespaces/netcdf/ncml-2.2">
  <aggregation dimName="time" type="joinExisting">
    <netcdf location="jan.nc" />
    <netcdf location="feb.nc" />
  </aggregation>
</netcdf>
 ```
 
 In the NCML file we can see the dimension we what to join on ("time"), and the netcdf files we are aggregating together ("jan.nc", "feb.nc"). We could now open this NCML files in something like toolsui (toolsui.jar is a java application available from unidata's website, great for viewing netcdf files)


3) With the NCML file generated we just need to add it to the THREDDS catalog, this can be done the same as any other netcdf dataset.

## ChangeLog

### RC8
* Better/automatic workaround for aggregation caching problem
* Bug fixes

### RC7
* Testing refactor
* Better class names
* Lots of cleanup in code and comments
* Now uses JDOM and XML objects instead of strings
* Jenkins integration

### RC6
* Testing cleanup
* Lots of bug schema related bug fixes

### RC5
* IOOS SWE Milestone 1.0 support
* Code cleanup and documentation
* Hosten in Maven
* Caching bug fix (see _Known Issues_ below)

### RC3-4
* Updated DescribeSensor formatting to match new IOOS DescribeSensorPlatform and DescribeSensorNetwork
* Added new response format for time series datasets
* New IOOS reponse format for GetObservation requests
* Templates for new IOOS formats found here: https://code.google.com/p/ioostech/source/browse/trunk/templates/Milestone1.0
* New system for logging missing variables and attributes in datasets 

### RC2:
* Expanded metadata reporting from files.
* Began updating the responses to the SOS 2.0 response format.
* Fixed formatting.
* Fixed many, many smaller bugs.
* Added error handling for large datasets.

### RC1:
* Added support for GetCapabilities, DescribeSensor and GetObservation requests.
* Added caching for GetCapabilities requests
* Supports CF 1.6 convention files including: TimeSeries, TimeSeriesProfile, Trajectory, Profile, TrajectoryProfile (Section) and Grid datasets.

##Known Issues
* Aggregating files using NcML does not work with the built in THREDDS caching system.  This is an issue on Unidata's side.  NcSOS has a built-in workaround that will disable caching when an aggregated dataset is detected.
