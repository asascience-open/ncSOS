#NcSOS - RC2

NcSOS adds an OGC SOS service to your existing THREDDS server.
You will need a working THREDDS installation of at least 4.3 to run NcSOS.

Visit http://github.com/asascience-open/ncSOS/wiki for installation and usage instructions.

##ChangeLog

###RC2:
Expanded metadata reporting from files.
Began updating the responses to the SOS 2.0 response format.
Fixed formatting.
Fixed many, many smaller bugs.
Added error handling for large datasets.

###RC1:
Added support for GetCapabilities, DescribeSensor and GetObservation requests.
Added caching for GetCapabilities requests
Supports CF 1.6 convention files including: TimeSeries, TimeSeriesProfile, Trajectory, Profile, TrajectoryProfile (Section) and Grid datasets.