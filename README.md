enviroCar Android App
=====================

This fork contains a Sensor Service for detecting emergency brakes by using the Accelerometer and OBD-II-Data.

Developed at the Institute for Geoinformatics in Münster by

* Simon Schoemaker (@Simon90)
* Daniel Schumacher (@Schumi09)
* Julius Wittkopp (@MrWitt)

in scope of the Spatio-temporal Analysis of Floating Car Data course project.

The SensorEventlistener calculates the G-Force for all combined accelerometer-axes. After reaching a specific threshold (1.2g) the current car’s data gets observed and stored for 15 seconds. Once the measuring process is finished the data gets analysed and the maximum braking distance and accordingly ts maximum braking duration is calculated for the car’s speed at event start. In case the car stops fully down to 0 km/h within this interval an emergency brake event is triggered. For now all triggered events get stored within one .txt-file for each every drive with the belonging measurements and evaluation whether it is an emergency break. In order to ignore potential multiple emergency break events right after the first (e.g. due to antilock braking system) only one analysis is performed. 