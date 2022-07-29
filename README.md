# MeetUp

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
Social media for startups. Users can register their startups and post updates, as well as discover new ones and collaborate with them.

### Description
This social media app has the purpose of bringing various project ideas from different people together. Users post their startup and smaller projects within the startup according to a template. An overview of each startup shows up in other users' feed according to a recommendation algorithm based on user preferences. Users can like/save their favorite startups, collaborate, and merge them (if 2 users have similar ideas and think their startups would perform better together).


https://user-images.githubusercontent.com/78447797/181845112-cbeab104-adfd-40cf-8377-baa2c9171da5.mp4


### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:** Productivity/Social
- **Mobile:** Check out latest startup ideas and get updates on them.
- **Story:** Fosters collaboration between people who are working on startups and helps them get exposed to new ideas just by checking their phone.
- **Market:** Anyone who is working on a startup or looking to get involved with one, as well as investors.
- **Habit:** Users are posting updates in the app as they make new developments at their startup and other users get notified of these updates, which they get to check out
- **Scope:** V1 incorporates register new startup, post updates and display it to other users. V2 allows users fiter the feed according to category. V3 incorporates maps and filtering according to location.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can create a new account
* User can login
* User can post a new startup according to a template
* User can view a feed of startups
* User can filter according to category
* User can see map of startups

### 2. Screen Archetypes

* Register
   * User can create a new account
* Login
   * User can login
* Feed
  * User can view a feed of startups
* Map
  * User can see map with startups
* Create
  * User can post a new startup according to a template
* Filter
  * User can filter according to name, category, keyword, distance


### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home Feed
* Post startup
* Filter startups
* Maps

**Flow Navigation** (Screen to Screen)

* Login Screen => Feed
* Registration Screen => Feed
* Feed => Detailed description
* Creation Screen => Home (after you finish posting the startup/update)

## Wireframes
![wireframes](https://user-images.githubusercontent.com/78447797/173667467-959d6cf3-1b66-4e83-a2a9-21942dbdf2a1.jpeg)

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 

<table>
  <tr>
   <td>Post/Startup
   </td>
   <td>
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>property
   </td>
   <td>type
   </td>
   <td>description
   </td>
  </tr>
  <tr>
   <td>object_id
   </td>
   <td>String
   </td>
   <td>unique id for post(default field)
   </td>
  </tr>
  <tr>
   <td>author
   </td>
   <td>Pointer to User
   </td>
   <td>post author
   </td>
  </tr>
  <tr>
   <td>name
   </td>
   <td>String
   </td>
   <td>name of startup
   </td>
  </tr>
  <tr>
   <td>Location(s)
   </td>
   <td>Array of string/pointer to map?
   </td>
   <td>locations of offices / remote
   </td>
  </tr>
  <tr>
   <td>short intro
   </td>
   <td>string
   </td>
   <td>mission statement
   </td>
  </tr>
  <tr>
   <td>description
   </td>
   <td>string
   </td>
   <td>more detailed description
   </td>
  </tr>
  <tr>
   <td>startup logo
   </td>
   <td>File
   </td>
   <td>logo of startup
   </td>
  </tr>
  <tr>
   <td>teams
   </td>
   <td>Array of strings
   </td>
   <td>a list of teams, e.g. ui/ux, ML, security
   </td>
  </tr>
  <tr>
   <td>contact email
   </td>
   <td>string
   </td>
   <td>email
   </td>
  </tr>
  <tr>
   <td>phone number
   </td>
   <td>string
   </td>
   <td>phone
   </td>
  </tr>
</table>



<table>
  <tr>
   <td>User
   </td>
   <td>
   </td>
   <td>
   </td>
  </tr>
  <tr>
   <td>property
   </td>
   <td>type
   </td>
   <td>description
   </td>
  </tr>
  <tr>
   <td>username
   </td>
   <td>String
   </td>
   <td>username
   </td>
  </tr>
  <tr>
   <td>password
   </td>
   <td>String
   </td>
   <td>password
   </td>
  </tr>
  <tr>
   <td>profile image
   </td>
   <td>File
   </td>
   <td>profile image
   </td>
  </tr>
</table>


### Models
[Add table of models]
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
