# Skill Tree Documentation (Server)

## Overview

The Skill Tree backend is a RESTful API built with **Spring Boot** and **MongoDB**, designed to manage user data, skill hierarchies, achievements, activities, and social interactions.  
It integrates **Firebase Authentication** for secure access and provides endpoints for both personal and public skill trees.

### Tech Stack
- Java 17
- Spring Boot 3.x
- MongoDB Atlas
- Firebase Authentication
- Maven

### Architecture
The system follows a layered architecture:

Controllers expose REST endpoints, Services contain business logic, and Repositories interact with MongoDB.

## Models
The structures for MongoDB documents.

### User 
A single end-user in Skill Tree.
```java
ObjectId id;                // Unique identifier
String firebaseId;          // Id in Firebase Authentication
String displayName;         // Display Name (unique)
String email;               // Email
String profilePictureUrl;   // URL for hosted profile picture
Instant createdAt;          // Date created
Instant updatedAt;          // Date updated
```

### Tree
A grouping of related Skills and Achievements.
```java
ObjectId id;            // Unique identifier
ObjectId userId;        // Id of the owning User
String name;            // Name
String backgroundUrl;   // URL for hosted background picture
String description;     // Description
Visibility visibility;  // PRIVATE, FRIENDS, PUBLIC, PRESET
Instant createdAt;      // Date created
Instant updatedAt;      // Date updated
```

#### Visibility (Enum)
Determines which Users can see a Tree.
```java
PRIVATE,    // Only me
FRIENDS,    // Only my friends
PUBLIC,     // All Skill Tree Users
PRESET      // All Skill Tree Users (only for Trees in the Preset store)
```

### Skill
A Skill.
```java
ObjectId id;            // Unique identifier
ObjectId userId;        // References the User owning the Skill
ObjectId treeId;        // References the Tree the Skill belongs to
String name;            // The name of the Skill 
String backgroundUrl;   // The URL for the hosted background picture
double timeSpentHours;  // The amount of time the User has logged (via Activities) for this skill
ObjectId parentSkillId; // References the Skill that this Skill is a sub-Skill to
Instant createdAt;      // Date created
Instant updatedAt;      // Date updated
```

#### SkillSortMode 
Decides the order in which Skills are displayed in a list.
```java
CREATED_AT,
TIME_SPENT,
RECENTLY_USED,
NAME
```

### Achievement 
An Achievement.
```java
ObjectId id;                    // Unique identifier
ObjectId userId;                // References the User owning this Achievement
ObjectId treeId;                // References the Tree this Achievement belongs to
String title;                   // Title
String backgroundUrl;           // The URL for the hosted background picture
String description;             // Description
List<ObjectId> prerequisites;   // Achievements that require completion before this one
boolean complete;               // Completion status
Instant completedAt;            // Date completed
Instant createdAt;              // Date created
Instant updatedAt;              // Date updated
```

#### AchievementSortMode 
Decides the order in which Achievements are displayed in a list.
```java
TITLE,
COMPLETED_AT,
CREATED_AT
```

### Orientation
Represents the visual layout of a Tree. 
```java
ObjectId id;                                    // Unique identifier
ObjectId userId;                                // References the User owning this Orientation
ObjectId treeId;                                // References the Tree this Orientation belongs to
List<SkillLocation> skillLocations;             // Locations for all Skills in the Tree
List<AchievementLocation> achievementLocations; // Locations for all Achievements in the Tree
Instant createdAt;                              // Date created
Instant updatedAt;                              // Date updated
```

#### SkillLocation 
The location of a Skill in a Tree.
```java
ObjectId skillId;   // Id of the Skill
double x;           // x location (ratio)
double y;           // y location (ratio)
```

#### AchievementLocation 
The location of an Achievement in a Tree.
```java
ObjectId achievementId; // Id of the Achievement
double x;               // x location (ratio)
double y;               // y location (ratio)
```

### Activity 
An activity completed by the User that used one or more of their Skills.
```java
ObjectId id;                    // Unique identifier
ObjectId userId;                // References the User owning this Activity
String name;                    // Name
String description;             // Description
double duration;                // How long the Activity was (hours)
List<SkillWeight> skillWeights; // List of Skills used and their importance to the Activity
Instant createdAt;              // Date created
Instant updatedAt;              // Date updated
```

#### SkillWeight
The proportion of how much a specific Skill was used.
```java
ObjectId skillId;   // The Id of the Skill
double weight;      // The weight of the Skill with respect to the Activity (0.0 - 1.0)
```

### Friendship
A friend relationship between two users.
```java
ObjectId id;                // Unique identifier
ObjectId requesterId;       // Id of the requesting User (sender)
ObjectId addresseeId;       // Id of the addressed User (receiver)
FriendRequestStatus status; // Status of the Friendship
Instant createdAt;          // Date created
Instant updatedAt;          // Date updated
```

#### FriendRequestStatus 
The status of a Friendship relationship/request.
```java
ACCEPTED,
PENDING,
BLOCKED
```

## Authentication 
Authentication is implemented with Firebase Authentication. Endpoints are protected by matching the Firebase Id from the Java Web Token to the firebaseId attribute in a User entity.

## Endpoints 

API information can be viewed by pasting the OpenAPI spec, `skilltree_api.yaml`, into [Swagger Editor](https://editor.swagger.io/). 

## Data Transfer Objects (DTOs)

Data Transfer Objects for the Skill Tree API were designed with two things in mind: **Security & Response Efficiency**. As a result, sensitive information like Firebase Ids, emails, and unique identifiers for some objects are hidden from the end user. Also, some DTOs were created to hold aggregate data in order to reduce the number of API calls and data transferred over the wire.

Most entities have both a Request and a Response DTO, often containing the same fields. Combining the two structures into a single DTO was avoided with the intention of keeping the codebase clean. For the sake of not cluttering this documentation, **only DTOs with a less trivial structure are shown below.**

---

### AchievementFeedItem

**Purpose:** Represents a single achievement completion card displayed in the friend actions feed. Used to show when friends complete achievements in their skill trees.

#### JSON Example
```json
{
  "type": "ACHIEVEMENT",
  "postedAt": "2025-10-09T14:30:00Z",
  "displayName": "John Doe",
  "profilePictureUrl": "https://example.com/profiles/johndoe.jpg",
  "title": "Marathon Runner",
  "backgroundUrl": "https://example.com/achievements/marathon.jpg",
  "description": "Complete a full marathon"
}
```

#### Fields
- **type**: Always `"ACHIEVEMENT"`
- **postedAt**: ISO 8601 timestamp when the achievement was completed
- **displayName**: User's display name
- **profilePictureUrl**: URL to user's profile picture
- **title**: Achievement title
- **backgroundUrl**: URL to achievement's background image
- **description**: Achievement description

---

### ActivityFeedItem

**Purpose:** Represents a single activity card displayed in the friend actions feed. Shows when friends log new activities with their associated skills and time spent.

#### JSON Example
```json
{
  "type": "ACTIVITY",
  "postedAt": "2025-10-09T15:00:00Z",
  "displayName": "Jane Smith",
  "profilePictureUrl": "https://example.com/profiles/janesmith.jpg",
  "name": "Morning Run",
  "duration": 1.5,
  "description": "5 mile run through the park",
  "weightedSkills": [
    {
      "weight": 0.7,
      "skillName": "Cardio",
      "backgroundUrl": "https://example.com/skills/cardio.jpg"
    },
    {
      "weight": 0.3,
      "skillName": "Endurance",
      "backgroundUrl": "https://example.com/skills/endurance.jpg"
    }
  ]
}
```

#### Fields
- **type**: Always `"ACTIVITY"`
- **postedAt**: ISO 8601 timestamp when activity was logged
- **displayName**: User's display name
- **profilePictureUrl**: URL to user's profile picture
- **name**: Activity name
- **duration**: Duration in hours
- **description**: Activity description
- **weightedSkills**: Array of skills with their contribution weights (0-1) to this activity

---

### TreeFeedItem

**Purpose:** Represents a skill tree creation card in the friend actions feed. Shows when friends create new skill trees.

#### JSON Example
```json
{
  "type": "TREE",
  "postedAt": "2025-10-09T10:00:00Z",
  "displayName": "Alice Johnson",
  "profilePictureUrl": "https://example.com/profiles/alice.jpg",
  "name": "Fitness Journey",
  "description": "My path to becoming healthier",
  "backgroundUrl": "https://example.com/trees/fitness.jpg"
}
```

#### Fields
- **type**: Always `"TREE"`
- **postedAt**: ISO 8601 timestamp when tree was created
- **displayName**: User's display name
- **profilePictureUrl**: URL to user's profile picture
- **name**: Tree name
- **description**: Tree description
- **backgroundUrl**: URL to tree's background image

---

### TreeLayout

**Purpose:** Contains minimal rendering information for displaying a complete skill tree. Maps skill names and achievement titles to their layout data for frontend rendering.

#### JSON Example
```json
{
  "skillLayout": {
    "Programming": {
      "parentSkill": null,
      "timeSpentHours": 120.5,
      "backgroundUrl": "https://example.com/skills/programming.jpg",
      "x": 100.0,
      "y": 50.0
    },
    "JavaScript": {
      "parentSkill": "Programming",
      "timeSpentHours": 45.0,
      "backgroundUrl": "https://example.com/skills/js.jpg",
      "x": 150.0,
      "y": 100.0
    }
  },
  "achievementLayout": {
    "First Commit": {
      "prerequisites": [],
      "description": "Make your first git commit",
      "backgroundUrl": "https://example.com/achievements/first-commit.jpg",
      "complete": true,
      "completedAt": "2025-09-15T12:00:00Z",
      "x": 200.0,
      "y": 75.0
    },
    "100 Commits": {
      "prerequisites": ["First Commit"],
      "description": "Reach 100 commits",
      "backgroundUrl": "https://example.com/achievements/100-commits.jpg",
      "complete": false,
      "completedAt": null,
      "x": 250.0,
      "y": 75.0
    }
  }
}
```

#### Fields
- **skillLayout**: Map where keys are skill names, values are SkillLayout objects
- **achievementLayout**: Map where keys are achievement titles, values are AchievementLayout objects

---

### AchievementLayout

**Purpose:** Defines how a single achievement should be rendered in a skill tree, including position, completion status, and prerequisites.

#### JSON Example
```json
{
  "prerequisites": ["Beginner Badge", "First Steps"],
  "description": "Complete 10 activities in a single week",
  "backgroundUrl": "https://example.com/achievements/weekly-warrior.jpg",
  "complete": true,
  "completedAt": "2025-10-01T08:30:00Z",
  "x": 300.0,
  "y": 150.0
}
```

#### Fields
- **prerequisites**: Array of achievement titles that must be completed first
- **description**: Achievement description
- **backgroundUrl**: URL to achievement's background image
- **complete**: Boolean indicating completion status
- **completedAt**: ISO 8601 timestamp of completion (null if not complete)
- **x**: X-coordinate for rendering position
- **y**: Y-coordinate for rendering position

---

### SkillLayout

**Purpose:** Defines how a single skill should be rendered in a skill tree, including hierarchy, time invested, and position.

#### JSON Example
```json
{
  "parentSkill": "Athletics",
  "timeSpentHours": 25.5,
  "backgroundUrl": "https://example.com/skills/running.jpg",
  "x": 175.0,
  "y": 225.0
}
```

#### Fields
- **parentSkill**: Name of the parent skill (null for root skills)
- **timeSpentHours**: Total hours logged for this skill through activities
- **backgroundUrl**: URL to skill's background image
- **x**: X-coordinate for rendering position
- **y**: Y-coordinate for rendering position

---

### FavoriteTree

**Purpose:** Combines tree information with statistics for the owning user. Provides a summary view of a user's skill tree with progress metrics. Only sent to the tree owner.

#### JSON Example
```json
{
  "treeId": "507f1f77bcf86cd799439011",
  "name": "Career Development",
  "backgroundUrl": "https://example.com/trees/career.jpg",
  "totalTimeLogged": 342.75,
  "totalSkills": 15,
  "totalAchievements": 25,
  "achievementsCompleted": 12
}
```

#### Fields
- **treeId**: MongoDB ObjectId of the tree
- **name**: Tree name
- **backgroundUrl**: URL to tree's background image
- **totalTimeLogged**: Total hours logged across all activities in this tree
- **totalSkills**: Total number of skills in the tree
- **totalAchievements**: Total number of achievements in the tree
- **achievementsCompleted**: Number of completed achievements

---

### FriendList

**Purpose:** Represents all friendship relationships for a user, categorized by status. Provides a complete view of friend requests, accepted friends, and blocked users.

#### JSON Example
```json
{
  "incoming": [
    {
      "displayName": "Bob Wilson",
      "profilePictureUrl": "https://example.com/profiles/bob.jpg"
    }
  ],
  "outgoing": [
    {
      "displayName": "Carol Davis",
      "profilePictureUrl": "https://example.com/profiles/carol.jpg"
    }
  ],
  "friends": [
    {
      "displayName": "David Lee",
      "profilePictureUrl": "https://example.com/profiles/david.jpg"
    },
    {
      "displayName": "Emma White",
      "profilePictureUrl": "https://example.com/profiles/emma.jpg"
    }
  ],
  "blocked": [
    {
      "displayName": "Spammer User",
      "profilePictureUrl": "https://example.com/profiles/default.jpg"
    }
  ]
}
```

#### Fields
- **incoming**: Array of UserResponse objects for pending incoming friend requests
- **outgoing**: Array of UserResponse objects for pending outgoing friend requests
- **friends**: Array of UserResponse objects for accepted friends
- **blocked**: Array of UserResponse objects for blocked users

---

### LeaderboardEntry

**Purpose:** Represents a single user's position and statistics on a leaderboard. Typically sent in arrays for ranking users by time logged or achievements completed.

#### JSON Example
```json
{
  "displayName": "Sarah Martinez",
  "profilePictureUrl": "https://example.com/profiles/sarah.jpg",
  "timeLogged": 156.25,
  "achievementsCompleted": 34
}
```

#### Fields
- **displayName**: User's display name
- **profilePictureUrl**: URL to user's profile picture
- **timeLogged**: Total hours logged across all activities
- **achievementsCompleted**: Total number of achievements completed

---

### RecentActivity

**Purpose:** Tracks a user's activity streak and daily activity counts over a recent period. Used to display engagement metrics and encourage consistent activity logging.

#### JSON Example
```json
{
  "streak": 7,
  "dailyActivityCounts": {
    "2025-10-09": 3,
    "2025-10-08": 2,
    "2025-10-07": 1,
    "2025-10-06": 4,
    "2025-10-05": 2,
    "2025-10-04": 1,
    "2025-10-03": 3
  }
}
```

#### Fields
- **streak**: Number of consecutive days with at least one activity logged
- **dailyActivityCounts**: Map of dates (ISO 8601 format) to number of activities logged on that day

