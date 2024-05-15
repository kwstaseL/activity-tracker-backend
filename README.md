
# Activity Tracker (Backend)

## Overview

This project is a distributed app developed for the course of Distributed Systems (INF507 - 3664) at Athens University of Economics and Business. It provides users with a comprehensive tool for analyzing their activity tracking data.

The system consists of a mobile [frontend](https://github.com/kwstaseL/Activity-Tracker) application for the UI and a backend system for data analysis (contained in this repository).

## Backend System

The backend system processes the recorded data using the [MapReduce](https://en.wikipedia.org/wiki/MapReduce) framework, with one master node and multiple worker nodes. The workers are used as both map and reduce workers, to simplify the process. The user is expected to send gpx files to the master node (through the use of the frontend app), which are then mapped to workers for calculations.

This allows users to gain insights into their activities and compare them with others who engage in similar activities. Users can maintain a personal profile, add activities, and access statistics such as the number of activities, total distance, and exercise time.

## Features

In addition to the core functionality, extra features were implemented such as:

**Segment Detection:** The backend system contains gpx files that correspond to segments, which it loads on initialization. If a route a user records contains a segment, the statistics for that activity are capturesd and stored in leaderboards. The system then displays the rankings and statistics for each segment, enabling users to track their progress.

## Usage

1. Clone the project
2. Configure the configuration file:

    • Locate the configuration file in the project directory.

    • Open the configuration file and update the value of the master_ip property to the appropriate IP address where the master node will be running.
3. Run the master node to begin accepting connections.
4. Run the worker(s) to make them available for processing tasks.

Once you have completed the backend setup, you can proceed with the frontend setup that can be found [here](https://github.com/kwstaseL/Activity-Tracker).

## Collaborators

- [hvlkk](https://www.github.com/hvlkk)
- [kwstaseL](https://www.github.com/kwstaseL)

## License

This project is licensed under the [MIT](https://choosealicense.com/licenses/mit/) License.
