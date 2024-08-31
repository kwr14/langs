def count_up_to(max_value):
    count = 1
    while count <= max_value:
        yield count
        count += 1


# Example usage of the generator
for number in count_up_to(5):
    print(number)


# create main function
