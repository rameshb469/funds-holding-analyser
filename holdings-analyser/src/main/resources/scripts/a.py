import csv

input_file = 'FCM_INTRM_BC12092025.DAT'
output_file = 'FCM_INTRM_BC12092025.csv'

with open(input_file, 'r') as infile, open(output_file, 'w', newline='') as outfile:
    writer = csv.writer(outfile)
    for line in infile:
        fields = [field.strip() for field in line.strip().split('|')]
        writer.writerow(fields)
