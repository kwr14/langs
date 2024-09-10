userNames = ["User1", "User2", "User3", "User4", "User5"]

print("Welcome to AQA User Registration")

userName = input("New username: ").strip()

if userName in userNames:
    print(f"{userName} already exists. Please try a different username.")
else:
    userPassword = input(
        "Password (12 characters or more, include numbers and special characters): "
    ).strip()

    if len(userPassword) < 12:
        print("Password is too short. It must be at least 12 characters.")
    else:
        print(f"User {userName} has been successfully registered.")
