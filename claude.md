I want to create a scheduler project in this directory using current source code structure and setup. Please add these features:

1. Scheduling Job: System can setup and define a scheduler based on the input with CRON format. 
2. Delaying Job: System can setup a delayed job that executed one time only. It works like an reminder to a user to do something

The input is triggered by both API and Message Event Listener. I don't define what Message provider I would use, but please create at least the interface of it. 

Don't forget to add the integration test, and for now please test by hit the API only