# Activity-Tracker

Overview

This project is a simple system for analyzing activity tracking data using the MapReduce framework. 
The system consists of a mobile frontend application for recording activity and a backend system which handles the analysis of the data. 
The system allows users to maintain a personal profile, where they can add their activities, 
which can then be analyzed through the application and compared with other users in similar activities. 
Users can also see their overall statistics, such as the number of activities, total distance, total exercise time, etc.


Configuration

Before running the app, there are a few configuration steps you need to take:

    > Configure the master's IP address in the configuration file. By default, the app will run on localhost.
    > Ensure that the "available_gpx" and "registered_segments" folders exist and contain the user's GPX files and segments, respectively.

Usage

To use the Activity-Tracker app, follow these steps:

   > Start the master server to begin accepting connections.
   > Start the workers and specify the maximum number of workers in the configuration file.
   > Start the user client to send and start processing GPX files and segments.
