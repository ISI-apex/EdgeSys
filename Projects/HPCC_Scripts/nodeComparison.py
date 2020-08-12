# Copy https://hpcc.usc.edu/support/infrastructure/node-allocation/

# Grep for all nodes that start with hpc

# Expand nodelist




# ****TO USE


	# sinfo -N > nodeList.txt
	# tail -n +2 nodeList.txt > nodeList2
	# sed 's/ .*$//' nodeList2 > nodeList3
	# # Manual delete text names
	# sort nodeList3 | uniq > nodeList4
	# # Manual delete junk from beginning
	# sed 's/hpc//g' nodeList4 > nodeList5
	# This will print out the  exclude nodes in the last line


#*****



# sinfo -N > nodeList.txt
# 	Delete header
# 		tail -n +2 nodeList.txt > nodeList2
# 	Strip out everything after node name
# 		sed 's/ .*$//' nodeList2 > nodeList3
# 	Delete nodes with text names

# 	Delete duplicates
# 		sort nodeList3 | uniq > nodeList4

# Delete junk from beginning
# Delete hpc from lines






import sys



scriptExcludeTuples=[
# (965,972),
# (981,1021),
# (1028,1050),
# (1123,1128),
# (1196,1200),
# (1223,1230),
# (1118,1122),
# (1407,1414),
# (2726,2729),
# (2758,2761),
# (3025,3027),
# (3031,3264),
# (3520,3527),
# (3591,3594),
# (3598,3600),
# (3648,3688),
# (3766,3768),
# (3606,3607),
# (3817,3834),
# (4129,4176),
# (4323,4324),
# (4331,4374),
# (4433,4520),
# (4570,4573),
# (4623,4632),
# (4657,4674),
# (4578,4616),
]

scriptExcludeSingles=[
# 3852,
# 4522,
# 4523,
]



okayNodeTuples = [
(3769,3792),
(3938,3939),
(3947,3949),
(3987,4008),
(4097,4104),


# (3817,3834),
# (3852),
]



def main():
	okayNodes=[]

	okayNodes.append(3966)
	okayNodes.append(3888)
	for nodeTuple in okayNodeTuples:
		for i in range(nodeTuple[0], nodeTuple[1]+1):
			okayNodes.append(i)

	excludeNodes = []
	for excludeNodeTuple in scriptExcludeTuples:
		for i in range(excludeNodeTuple[0], excludeNodeTuple[1]+1):
			excludeNodes.append(i)
	for excludeSingle in scriptExcludeSingles:
		excludeNodes.append(excludeSingle)

	print excludeNodes

	print "File: " + sys.argv[1]
	with open(sys.argv[1]) as f:
		fileNodeData = f.readlines()
	fileNodes = [int(x.strip()) for x in fileNodeData]
	print fileNodes

	print set(excludeNodes)- set(fileNodes)
	print set(fileNodes)- set(excludeNodes)
	print set(fileNodes) - set(excludeNodes) - set(okayNodes)
	# createRange(set(fileNodes) - set(excludeNodes) - set(okayNodes))
	createRange(set(fileNodes) - set(okayNodes))


def createRange(inputSet):
	intervals=[]
	start=None
	current=None
	for node in inputSet:
		if start==None:
			start=node
			current=start
		else:
			if node==current+1:
				current=node
			else:
				if(start==current):
					print "hpc%04d,"%(start),
				else:
					print "hpc[%04d-%04d],"%(start, current),
				start=node
				current=node
	if(start==current):
		print "hpc%04d,"%(start),
	else:
		print "hpc[%04d-%04d],"%(start, current),
	print "hpc-1t-[1-4],hpc-lms[22-23]"
if __name__ == '__main__':
	main()


