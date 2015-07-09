#!/usr/local/bin/python
# -*- coding: utf-8 -*-
import sqlite3
import os
import codecs

def createDb(dbfilename):
	if (os.path.isfile(dbfilename)):
		os.remove(dbfilename)

	conn = sqlite3.connect(dbfilename)
	c = conn.cursor()


	c.execute('''CREATE TABLE destinations (name text)''')
	print "'destinations' table is created"
	c.execute('''CREATE TABLE paths (type integer, displaynumber integer, 
					pointA integer, pointB integer, pointC integer)''')
	print "'paths' table is created"
	c.execute('''CREATE TABLE reversepaths (destinationId integer, pathIds text)''')
	print "'reversepaths' table is created"
	
	printLines()

	conn.commit()
	conn.close()

def fillDb(dbfilename, csvfile):
	with codecs.open(csvfile, 'r', encoding='utf8') as f:

		conn = sqlite3.connect(dbfilename)
		c = conn.cursor()

		destinations = {}
		reversepaths = {}

		counter = 0
		for line in f:
			if counter == 0:
				counter = 1
				continue;

			chunks = line.split(',')
			# transport type is 1 if it is BUS, 0 if it is marshrutka
			transportType = 1 if chunks[1] == 'TRUE' else 0
			displaynumber = int(chunks[2])

			points = [chunks[3].strip(), chunks[4].strip(), chunks[5].strip()]

			for i in xrange(3):
				if points[i] and not destinations.has_key(points[i]):
					destinations[points[i]] = counter
					# print destinations[points[i]], points[i]
					counter += 1

			point_ids = [0]*3
			for i in xrange(3):
				point_ids[i] = -1 if not points[i] else destinations[points[i]]

			# print line
			# print points
			# print point_ids
			# print 'type=', transportType, ', displaynumber=',displaynumber

			c.execute('''INSERT INTO paths (type, displaynumber, pointA, pointB, pointC)
						 VALUES (?, ?, ?, ?, ?)''', \
						  (transportType, displaynumber, point_ids[0], point_ids[1], point_ids[2]))

			lastrowid = c.lastrowid


			for i in xrange(3):
				if points[i]:
					if reversepaths.has_key(point_ids[i]):
						reversepaths[point_ids[i]].append(lastrowid)
					else:
						reversepaths[point_ids[i]] = [lastrowid]

		destination_arr = [''] * len(destinations)
		for name in destinations.keys():
			destination_arr[destinations[name]-1] = name

		for name in destination_arr:
			#print name, type(name)
			c.execute('''INSERT INTO destinations (name) VALUES (?)''', (name,))

		printLines()

		for destId in reversepaths.keys():
			pathIds = map(str, reversepaths[destId])
			c.execute('''INSERT INTO reversepaths (destinationId, pathIds) VALUES (?, ?)''',\
							(destId, ','.join(pathIds)))
			print destId, ','.join(pathIds)

		conn.commit()
		conn.close()

def printData(dbfilename):
	conn = sqlite3.connect(dbfilename)
	c = conn.cursor()

	c.execute("SELECT ROWID, name FROM destinations")

	rows = c.fetchall()
	#points = [(x[0], x[1]) for x in rows]
	#points = sorted(points)
	
	points = []	
	
	printLines()

	for row in rows:
		points.append(row[1])
		print row[0], row[1]

	printLines()

	c.execute("SELECT ROWID, type, displaynumber, pointA, pointB, pointC FROM paths")

	rows = c.fetchall()

	for row in rows:
		print row[0], ('AVTOBUS' if row[1] == 1 else 'MARSHRUTKA'), 'N%d'%row[2], \
				points[row[3]-1], '-', points[row[4]-1], \
				'-' if row[5] != -1 else '', \
				points[row[5]-1] if row[5] != -1 else ''

	conn.commit()
	conn.close()

def printReverseData(dbfilename):

	conn = sqlite3.connect(dbfilename)
	c = conn.cursor()

	orayliqBazarId = getDestIdByName(c, u'Орайлық базар')
	askeriyGarnizonId = getDestIdByName(c, u'Әскерий гарнизон')
	xojanAwilId = getDestIdByName(c, u'Хожан аўыл')
	adayAwilId = getDestIdByName(c, u'Адай аўыл')

	points = getAllDestinationPoints(c)

	printLines()
	print u'Орайлық базарға баратуғын жөнелислер:'
	pathIds = getPathIdsByDestinationId(c, orayliqBazarId)
	rowCount = printPathsByPathIds(c, pathIds, points)

	printLines()
	print u'Әскерий гарнизонға баратуғын жөнелислер'
	pathIds = getPathIdsByDestinationId(c, askeriyGarnizonId)
	rowCount = printPathsByPathIds(c, pathIds, points)
	assert rowCount == 9

	printLines()
	print u'Хожан аўылға баратуғын жөнелислер'
	pathIds = getPathIdsByDestinationId(c, xojanAwilId)
	rowCount = printPathsByPathIds(c, pathIds, points)
	assert rowCount == 4

	printLines()
	print u'Адай аўылға баратуғын жөнелислер'
	pathIds = getPathIdsByDestinationId(c, adayAwilId)
	rowCount = printPathsByPathIds(c, pathIds, points)
	assert rowCount == 1

	printLines()
	print u'Орайлық туўыў үйинен Әскерий гарнизонға:'
	orayliqTuwiwUyiId = getDestIdByName(c, u'Орайлық туўыў үйи')
	pathIdsToOrayliqTuwiwUyi = getPathIdsByDestinationId(c, orayliqTuwiwUyiId)
	pathIdsToAskeriyGarnizon = getPathIdsByDestinationId(c, askeriyGarnizonId)
	intersection = set(pathIdsToOrayliqTuwiwUyi.split(',')) & set(pathIdsToAskeriyGarnizon.split(','))
	rowCount = printPathsByPathIds(c, ','.join(intersection), points)
	print 'Жөнелислер саны:', rowCount
	assert rowCount == 0

	printLines()
	print u'Орайлық базардан Темир Жол Вокзалына:'
	temirJolVokzali = getDestIdByName(c, u'Темир Жол Вокзалы')
	pathIdsToTemirJolVokzali = getPathIdsByDestinationId(c, temirJolVokzali)
	pathIdsToOrayliqBazar = getPathIdsByDestinationId(c, orayliqBazarId)
	intersection = set(pathIdsToTemirJolVokzali.split(',')) & set(pathIdsToOrayliqBazar.split(','))
	rowCount = printPathsByPathIds(c, ','.join(intersection), points)
	print 'Жөнелислер саны:', rowCount

	conn.commit()
	conn.close()


def getDestIdByName(cursor, name):
	cursor.execute("SELECT ROWID FROM destinations WHERE name = ?", [name])
	return cursor.fetchone()[0]

"""points array is indexed from 0, but ROWID in destinations starts from 1"""
def getAllDestinationPoints(cursor):
	cursor.execute("SELECT name FROM destinations")
	rows = cursor.fetchall()
	
	points = []	
	for row in rows:
		points.append(row[0])
	return points

def getPathIdsByDestinationId(cursor, destId):
	cursor.execute("SELECT pathIds FROM reversepaths WHERE destinationId = ?", [destId])
	return cursor.fetchone()[0]

def printPathsByPathIds(cursor, pathIds, points):
	cursor.execute("SELECT ROWID, type, displaynumber, pointA, pointB, pointC FROM paths WHERE ROWID IN ("+pathIds+")")
	rows = cursor.fetchall()

	for row in rows:
		print row[0], ('AVTOBUS' if row[1] == 1 else 'MARSHRUTKA'), 'N%d'%row[2], \
				points[row[3]-1], '-', points[row[4]-1], \
				'-' if row[5] != -1 else '', \
				points[row[5]-1] if row[5] != -1 else ''

	return len(rows)

def printLines():
	print "-" * 40

if __name__ == '__main__':
	createDb('marshrutka.db')
	fillDb('marshrutka.db', 'marshrutka.csv')
	printData('marshrutka.db')
	printReverseData('marshrutka.db')