def read_usernames():
    try:
        with open('usernames.txt', 'r') as file:
            return [line.strip() for line in file.readlines()]
    except FileNotFoundError:
        print("Error: 'usernames.txt' file not found.")
        exit()

def validate_username(userNames):
    attempt_count = 0
    max_attempts = 3
    while attempt_count < max_attempts:
        userName = input("New username: ").strip()
        if userName.lower() in (name.lower() for name in userNames):
            print(f"{userName} already exists. Please try a different username.")
            attempt_count += 1
        else:
            return userName
    print("Maximum username attempts exceeded.")
    exit()

def register_user(userName):
    attempt_count = 0
    max_attempts = 3
    while attempt_count < max_attempts:
        userPassword = input("Password (12 characters or more, include numbers and special characters): ").strip()
        if len(userPassword) < 12:
            print("Password is too short. It must be at least 12 characters.")
            attempt_count += 1
        else:
            print(f"User {userName} has been successfully registered.")
            return
    print("Maximum password attempts exceeded.")
    exit()

def main():
    print("Welcome to AQA User Registration")
    userNames = read_usernames()
    
    while True:
        print("\nMenu:")
        print("1. Validate Username")
        print("2. Register User")
        print("3. Exit")
        choice = input("Enter your choice: ")
        
        if choice == '1':
            userName = validate_username(userNames)
            if userName:
                print(f"Username {userName} is available.")
        elif choice == '2':
            userName = validate_username(userNames)
            if userName:
                register_user(userName)
        elif choice == '3':
            print("Exiting program.")
            break
        else:
            print("Invalid choice. Please select a valid option.")

if __name__ == "__main__":
    main()
