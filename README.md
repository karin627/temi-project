# Guidance Assistance System with Temi Robot

## Table of Contents
1. [Introduction](#introduction)
2. [Features](#features)
3. [Prerequisites](#prerequisites)
4. [Usage](#usage)
5. [Implementation](#implementation)

## Introduction
This system serves as a comprehensive aid for patients, especially the elderly, in navigating their entire medical journeys. The Android application is integrated into temi, a robotic AI assistant. By substituting specific temi SDK components, this application can be implemented on other robots that possess guiding capabilities and Android system. 

## Features
- Recommendation System: Recommend patients to several medical departments based on their orally described symptoms.
- Guidance: Lead patients to their designated locations.
- Notification: Send notification to the computer in the consulting room upon patients' arrival.

## Prerequisites
- Android Studio
- Python 3.11
- required python packages(recommendation):
  ```sh
  pip install -r requirements_recommendation.txt
  ```
- required python packages(notification):
  ```sh
  pip install -r requirements_notification.txt
  ```
- the computers running notification script have to be windows11

## Usage

### Android Application
Please build the application on your computer first. 
There are two ways to use this app on temi. 
1. Use ADB to connect your computer and temi to run this app on the temi.
2. Build apk file and install this app on the temi.
For the detailed steps, please refer to the manual of temi. 

### Python Scripts
Please run recommendation script on the computer in the lobby, and run notification script on each consulting room. 
1. **Recommendation Script**: `department_recommendation.py`
   ```sh
   python department_recommendation.py
   ```
2. **Notification Script**: `server_toast.py`
   ```sh
   python server_toast.py
   ```

## Implementation
For the detailed implementation of this system, please refer to the following introduction videos. 

Chinese version: https://youtu.be/zplzpIBm27I

English version: https://youtu.be/92ZnYM7VFg0
