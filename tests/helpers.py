import requests
import json
import random
import numpy as np

base_url = "http://localhost:8080"

tree_names = [
    "Computer Science", 
    "Software Development",
    "Fitness", 
    "Football", 
    "Soccer", 
    "Baseball", 
    "Hockey", 
    "Art", 
    "Cooking", 
    "Interior Design", 
    "Mechanical Engineering", 
    "Mathematics", 
    "Physics", 
    "Psychology", 
    "Finance", 
    "Networking", 
    "Chess", 
    "Body Building", 
    "Mental Health", 
    "Valorant", 
    "Counter Strike", 
    "R6 Siege", 
    "Bloons Tower Defense", 
    "Dog Sitting", 
    "Coaching", 
    "Studying", 
    "Typing", 
    "Blacksmithing", 
    "Necromancy", 
    "Wizardry", 
    "Game Development", 
    "Track and Field", 
    "Music"]

skill_names = [
    "Time Management",
    "Public Speaking",
    "Note Taking",
    "Critical Reading",
    "Creative Writing",
    "Data Analysis",
    "Personal Finance",
    "Guitar Fundamentals",
    "Strength Training",
    "Mindfulness",
    "Meal Planning",
    "Email Etiquette",
    "Debugging Technique",
    "UI Design Principles",
    "Database Modeling",
    "Networking & Relationships",
    "Project Planning",
    "Problem Decomposition",
    "Indoor Plant Care",
    "Running Form & Conditioning"
]

achievement_titles = [
    "Completed a Week of Habit Tracking",
    "Presented Without Notes",
    "Finished a Full Book",
    "Wrote Daily for 30 Days",
    "Ran a 5K",
    "Maintained Inbox Zero for 7 Days",
    "Completed First GitHub Project",
    "Cooked 10 Home Meals",
    "Woke Up at 6 AM for a Week",
    "Fixed a Major Bug Independently",
    "Completed 20 Workouts",
    "Published a Blog Post",
    "Learned a New Song Start to Finish",
    "Reduced Monthly Expenses by 10%",
    "Kept Plants Alive for 3 Months"
]

activity_names = [
    "Read textbook chapter",
    "Watched tutorial & took notes",
    "Practiced scales for guitar",
    "Wrote journal entry",
    "Did pomodoro coding session",
    "Went for a run",
    "Cooked meal from scratch",
    "Performed code review",
    "Reviewed flashcards",
    "Cleaned workspace",
    "Completed workout routine",
    "Practiced presentation aloud",
    "Reorganized personal notes",
    "Performed debugging on feature",
    "Studied vocabulary using spaced repetition"
]

first = ["Big", "Small", "Build", "Best", "OP", "Cool", "Epic", "Amazing", "Ok", "Splended", "Blended", "Clean", "Up", "Down", "Left", "Right", "Baller"]
last = ["Champ", "Goat", "Master", "Guy", "Bro", "Shake", "Pipe", "Twist", "Tooth", "Yolk", "Egg", "Shell", "Gas", "Wind", "Fella", "Turtle", "Dog", "Cat"]

BASE_URL = "http://localhost:8080"

def create_user_header():
    displayName = f"{random.choice(first)}{random.choice(last)}{random.randint(10, 99999)}"
    email = f"{displayName}@example.com"
    password = "qweruior8932@@@@jfie!slfdajil12345"
    
    res = requests.post(
        "http://localhost:9099/identitytoolkit.googleapis.com/v1/accounts:signUp?key=any", 
        json={
            'email': email, 
            'password': password, 
            'returnSecureToken': True
        }
    )
    
    res.raise_for_status()
    header = {"Authorization": f"Bearer {res.json()['idToken']}"}
    ures = requests.post(
        f"{BASE_URL}/api/users/me", 
        json={
            'displayName': displayName, 
            'profilePictureUrl': None
        }, 
        headers=header
    )    
    
    ures.raise_for_status()
    api_user = ures.json()
    return api_user, header
    
def create_tree(header):
    name = random.choice(tree_names)
    tree={
        "name": name,
        "backgroundUrl": None,
        "description": f'Everything about {name} is covered in this tree!',
        "visibility": "PRIVATE"
    }

    res = requests.post(
        f"{base_url}/api/trees/me",
        json = tree,
        headers=header
    )
    res.raise_for_status()
    print(f"helper: {res.json()}")
    return res.json()

def create_skill(tree, header):
    skill={
        "treeId": tree["id"],
        "name": random.choice(skill_names),
        "backgroundUrl": None,
        "timeSpentHours": 0,
        "parentSkillId": None
    }
    
    res = requests.post(
        f"{base_url}/api/skills/me",
        json=skill,
        headers=header
    )
    res.raise_for_status()
    return res.json()
    
def create_achievement(tree, header):
    achievement={
        "treeId": tree["id"],
        "title": random.choice(achievement_titles),
        "backgroundUrl": None,
        "description": "achievement description",
        "prerequisites": [], 
        "complete": False,
        "completedAt": None
    }
    
    res = requests.post(
        f"{base_url}/api/achievements/me",
        json=achievement,
        headers=header
    )
    res.raise_for_status()
    return res.json()

def create_activity(tree, header, skill_weights):
    activity={
        "name": random.choice(activity_names),
        "description": "this is what happened during the activity",
        "duration": random.random() * 11.5 + 0.5,
        "skillWeights": skill_weights
    }
    
    res = requests.post(
        f"{base_url}/api/activities/me",
        json=activity,
        headers=header
    )
    res.raise_for_status()
    return res.json()

def create_activity_full(tree, header):
    n = random.randint(2, 5)
    weights = np.random.dirichlet(np.ones(n))
    skill_weights = []
    for i in range(n):
        skill_weights.append({"skillId": create_skill(tree, header)['id'], "weight": weights[i]})
    return create_activity(tree, header, skill_weights)

        