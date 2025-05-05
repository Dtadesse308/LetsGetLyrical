# 310 Project Repository Information

This is the repository for the 310 group project.  This basic skeleton provides two initial branches:  **documentation** is for the group's Scrum process documentation, and **main** is the primary branch for development work as described in the project manual. 

## Brief Architecture Overview and Where Things Go
### React (Frontend)
All frontend code is to be located in `/site` or a subdirectory. `package.json` contains frontend build configuration and a list of packages your frontend needs to install in order to run. Place top-level pages in `/site/src/pages` and other components in `/site/src/components`.  You will need to download Node.js from https://nodejs.org/ if you want to run the frontend code independently. 

### SpringBoot (Backend)
All backend code is located in `/src/main` or a subdirectory. Please do not remove `redirect()` from `SpringBootAPI.java`, as this is responsible for enabling your website files to be served. 

## Running Tests and Checking Coverage for Java & JavaScript

All unit tests will run with `mvn test`  and the coverage output directory will be `target/site/jacoco` for your Java code and `site/coverage` for your JavaScript code.

### Frontend
To run just your frontend tests:
- First, navigate in your terminal/command prompt to `/site`.
- Run `npm run test` to run Jest tests
  - This will ask you to select an option: `a` will run all tests, `f` will run failed tests, etc
- Run `npm run test -- --coverage --watchAll=false` to run Jest coverage tests. Note the extra `--` is required.


## Running Your App Locally During Development

To run the app in the development environment, first run `mvn compile` and then `mvn spring-boot:run` The app will now be available on `http://localhost:8080`

### Frontend only
- Navigate to `/site` in terminal/command prompt
- Run `npm start`. Note that this will auto rebuild/refresh when you make changes to the frontend.

### Backend only
- Run the main method in `SpringBootAPI.java`

## Running Acceptance Tests & Configuring a Subset of Features to Run

To run the project's acceptance tests, use `mvn integration-test`.  Cucumber can be configured to run a subset of the features by modifying the `junit-platform.properties` file in the `src/test/resources` folder

