ExplainIt 🚀
============

[![License: CC BY-NC-SA](https://img.shields.io/badge/License-CC%20BY--NC--SA-blue.svg)]
[![Python](https://img.shields.io/badge/backend-Python%203.12-blue.svg)]
[![Kotlin](https://img.shields.io/badge/Android-Kotlin%201.8-blue.svg)]

**ExplainIt** is an experimental project combining Machine Learning (ML) and Android. 
It features a Python backend, ML modules, and a mobile client to provide intelligent user interaction.

--------------------------------------------------------------------------------
Contents
--------
- Description
- Features
- Architecture
- Installation
    - Backend
    - Android App
- Usage
- Examples
- Future Plans
- Contributing
- License
- Contact
- Acknowledgements

--------------------------------------------------------------------------------
Description
-----------
ExplainIt allows users to interact with ML models through an Android app.
- Python backend processes requests and communicates with ML pipelines
- Android client sends and receives data from the backend
- Modular ML pipeline allows adding new models and improving training

--------------------------------------------------------------------------------
Features
--------
- Python REST API backend
- Integration with ML models (inference, training)
- Android app written in Kotlin
- Lightweight structure for hackathons and prototyping
- Test and debug scripts (e.g., test_run.py)

--------------------------------------------------------------------------------
Architecture
------------
    Android Client  <-->  Backend API  -->  ML Pipeline

- Android Client: UI that sends requests to the backend
- Backend API: Receives requests, processes them, and forwards to ML pipeline
- ML Pipeline: Performs ML inference and returns results

--------------------------------------------------------------------------------
Installation
------------
### Backend
1. Clone repository:
    ```git clone https://github.com/luc1k1/ExplainIt.git```
    ```cd ExplainIt```

2. Create virtual environment:
    ```python3 -m venv venv```
   
    ```source venv/bin/activate```   # macOS/Linux
   
    ```# venv\Scripts\activate```    # Windows

4. Install dependencies:
    ```pip install -r requirements.txt```

5. Create .env file for API keys:
    ```GEMINI_API_KEY=your_api_key_here```

6. Test backend:
    ```python test_run.py```

### Android App
1. Open the 'app' folder in Android Studio
2. Ensure required SDK and Kotlin versions
3. Set backend API URL in code or build.gradle
4. Run on emulator or physical device

--------------------------------------------------------------------------------
Usage
-----
1. Open the Android app
2. Enter a text/query
3. The backend sends it to the ML model
4. Response is returned and displayed in the app

--------------------------------------------------------------------------------
Examples
--------
Input: "Explain quantum computing in simple terms."
Output: "Quantum computing is..."

--------------------------------------------------------------------------------
Future Plans
------------
- Automated model training
- New API endpoints (e.g., classification, translation)
- Improved Android UI/UX
- Support for multiple ML frameworks
- Cloud deployment for backend

--------------------------------------------------------------------------------
Contributing
------------
1. Fork the repository
2. Create a new branch: ```git checkout -b feature/your-feature```
3. Make changes and test
4. Create Pull Request with description

Please follow the Code of Conduct and add tests where possible.

--------------------------------------------------------------------------------
License
-------
Custom Non-Commercial License:

1. You may use and modify the code **for non-commercial purposes only**.  
2. Any modifications, enhancements, or derivative works **must be published back to this repository**.  
3. You may not sell, license, or distribute this code or derivative works for commercial purposes.  
4. All rights and credits to the original authors (luc1k1) must remain intact.  

By using this code, you agree to these terms.

--------------------------------------------------------------------------------
Contact
-------
Author: **luc1k1**  & **dyingangell**

GitHub: https://github.com/luc1k1  & https://github.com/dyingangell

--------------------------------------------------------------------------------
Acknowledgements
----------------
- Thanks to all contributors
- Inspiration from the ML and Android communities
