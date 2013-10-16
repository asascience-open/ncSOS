# NcSOS

Stable version: **RC6**

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

## ChangeLog

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
* Aggregating files using NcML does not work with the built in THREDDS caching system.  This is an issue on Unidata's side.  A temporary workaround is to set all of the `urlPath` attributes on your NcML aggregations to use the suffix `.ncml`.  NcSOS explicity disabled caching for datasets that end with `.ncml`.
