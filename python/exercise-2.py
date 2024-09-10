# Read existing usernames from a file
with open("usernames.txt", "r") as file:
    userNames = [line.strip() for line in file]

# Welcome message
print("Welcome to AQA User Registration")

# Initialize attempt counter and set maximum allowed attempts
attempt_count = 0
max_attempts = 3

# Username input loop
while attempt_count < max_attempts:
    userName = input("New username: ").strip()
    # Check if the username already exists, considering case insensitivity
    if userName.lower() in (name.lower() for name in userNames):
        print(f"{userName} already exists. Please try a different username.")
        attempt_count += 1  # Increment attempt counter
    else:
        # If username is unique, append it to the file
        with open("usernames.txt", "a") as file:
            file.write(userName + "\n")
        break  # Break loop if username is unique
else:
    # Inform user of too many failed attempts and exit program
    print("Maximum username attempts exceeded.")
    exit()

# Reset attempt count for password entry
attempt_count = 0

# Password input loop
while attempt_count < max_attempts:
    userPassword = input(
        "Password (12 characters or more, include numbers and special characters): "
    ).strip()
    # Check if the password meets the length requirement
    if len(userPassword) < 12:
        print("Password is too short. It must be at least 12 characters.")
        attempt_count += 1  # Increment attempt counter
    else:
        # Successful registration message
        print(f"User {userName} has been successfully registered.")
        break  # Break loop if password is valid
else:
    # Inform user of too many failed attempts and exit program
    print("Maximum password attempts exceeded.")
    exit()
