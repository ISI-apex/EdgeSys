

import sys
import json
import copy
import pprint


def main():
	if len(sys.argv)!= 4:
		print "Error, need arguments"
		return

	print sys.argv

	with open(sys.argv[1], "r") as jsonFile:
		data = json.load(jsonFile)


	for idx, cluster in enumerate(data):
		# Found existing, update in file and write
		if cluster["name"]==sys.argv[2]:
			data[idx]["zk"]=sys.argv[3]
			with open(sys.argv[1], "w") as jsonFile:
				json.dump(data, jsonFile)
				exit(0)

	print "Adding new entry"
	# pprint.pprint(data)
	data.append(copy.deepcopy(data[0]))
	# print
	# pprint.pprint(data)
	data[-1]["name"]=sys.argv[2]
	data[-1]["zk"]=sys.argv[3]
	# print
	# pprint.pprint(data)
	with open(sys.argv[1], "w") as jsonFile:
		json.dump(data, jsonFile)

	pass



if __name__ == '__main__':
	main()