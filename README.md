
# Activity Tracker (Backend)

# Overview

This project is a distributed app developed for the course Distributed Systems at AUEB. It provides users with a comprehensive tool for analyzing their activity tracking data. 

The system consists of a mobile [frontend](https://github.com/kwstaseL/Activity-Tracker) application for the UI and a backend system for data analysis.

# Backend System

The backend system processes the recorded data using the [MapReduce](https://en.wikipedia.org/wiki/MapReduce) framework. 

This allows users to gain insights into their activities and compare them with others who engage in similar activities. Users can maintain a personal profile, add activities, and access statistics such as the number of activities, total distance, and exercise time.


## Extra Features

In addition to the core functionality, extra features where implemented such as:

**Segment Detection**: The backend system captures users' segments from their activity routes and generates leaderboards that highlight the performance differences among users. 
It then displays the rankings and statistics for each segment, enabling users to track their progress.

## Usage

1. Clone the project
2. Open Intellij IDEA.
3. Configure the configuration file:

    • Locate the configuration file in the backend project directory.

    • Open the configuration file and update the value of the master_ip property to the appropriate IP address where the master             server will be running.
4. Run the master server to begin accepting connections.
5. Run the worker(s) to make them available for processing tasks.

Once you have completed the backend setup, you can proceed with the frontend setup that can be found [here](https://github.com/kwstaseL/Activity-Tracker).

## Collaborators

- [hvlkk](https://www.github.com/hvlkk)
- [kwstaseL](https://www.github.com/kwstaseL)




## License
This project is licensed under the [MIT](https://choosealicense.com/licenses/mit/) License.


