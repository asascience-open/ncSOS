#NcSOS - 4.3.16

NcSOS adds an OGC SOS service to your existing THREDDS server.

You will need a working THREDDS installation of at least 4.3.16 to run NcSOS.

Visit http://github.com/asascience-open/ncSOS/wiki for installation and usage instructions.

##ChangeLog

###4.3.16
* Version number aligns with THREDDS versioning
* Updated DescribeSensor formatting to match new IOOS DescribeSensorPlatform and DescribeSensorNetwork
* Added new response format for time series datasets
* New IOOS reponse format for GetObservation requests
* Templates for new IOOS formats found here: https://code.google.com/p/ioostech/source/browse/trunk/templates/Milestone1.0
* New system for logging missing variables and attributes in datasets 

###RC2:
* Expanded metadata reporting from files.
* Began updating the responses to the SOS 2.0 response format.
* Fixed formatting.
* Fixed many, many smaller bugs.
* Added error handling for large datasets.

###RC1:
* Added support for GetCapabilities, DescribeSensor and GetObservation requests.
* Added caching for GetCapabilities requests
* Supports CF 1.6 convention files including: TimeSeries, TimeSeriesProfile, Trajectory, Profile, TrajectoryProfile (Section) and Grid datasets.

##Known Issues
* Aggregate NCML files stored in cache are not able to be read subsequently. This is an issue on Unidata's side, temp fix is to disable the NetCDFFileCache: http://www.unidata.ucar.edu/projects/THREDDS/tech/reference/ThreddsConfigXMLFile.html.