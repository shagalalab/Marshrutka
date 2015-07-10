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


	c.execute('''CREATE TABLE destinations (_id integer primary key, name text)''')
	print "'destinations' table is created"
	c.execute('''CREATE TABLE routes (_id integer primary key, type integer, displaynumber integer, 
					pointA integer, pointB integer, pointC integer)''')
	print "'routes' table is created"
	c.execute('''CREATE TABLE reverseroutes (_id integer primary key, destinationId integer, routeIds text)''')
	print "'reverseroutes' table is created"

	# for use in android
	c.execute('''CREATE TABLE "android_metadata" ("locale" TEXT DEFAULT 'en_US')''')
	c.execute('''INSERT INTO "android_metadata" VALUES ('en_US')''')
	
	printLines()

	conn.commit()
	conn.close()

def fillDb(dbfilename, csvfile):
	with codecs.open(csvfile, 'r', encoding='utf8') as f:

		conn = sqlite3.connect(dbfilename)
		c = conn.cursor()

		destinations = {}
		reverseroutes = {}

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

			c.execute('''INSERT INTO routes (type, displaynumber, pointA, pointB, pointC)
						 VALUES (?, ?, ?, ?, ?)''', \
						  (transportType, displaynumber, point_ids[0], point_ids[1], point_ids[2]))

			lastrowid = c.lastrowid


			for i in xrange(3):
				if points[i]:
					if reverseroutes.has_key(point_ids[i]):
						reverseroutes[point_ids[i]].append(lastrowid)
					else:
						reverseroutes[point_ids[i]] = [lastrowid]

		destination_arr = [''] * len(destinations)
		for name in destinations.keys():
			destination_arr[destinations[name]-1] = name

		for name in destination_arr:
			#print name, type(name)
			c.execute('''INSERT INTO destinations (name) VALUES (?)''', (name,))

		printLines()

		for destId in reverseroutes.keys():
			routeIds = map(str, reverseroutes[destId])
			c.execute('''INSERT INTO reverseroutes (destinationId, routeIds) VALUES (?, ?)''',\
							(destId, ','.join(routeIds)))
			print destId, ','.join(routeIds)

		conn.commit()
		conn.close()

def printData(dbfilename):
	conn = sqlite3.connect(dbfilename)
	c = conn.cursor()

	c.execute("SELECT _id, name FROM destinations")

	rows = c.fetchall()
	#points = [(x[0], x[1]) for x in rows]
	#points = sorted(points)
	
	points = []	
	
	printLines()

	for row in rows:
		points.append(row[1])
		print row[0], row[1]

	printLines()

	c.execute("SELECT _id, type, displaynumber, pointA, pointB, pointC FROM routes")

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
	routeIds = getRouteIdsByDestinationId(c, orayliqBazarId)
	rowCount = printRoutesByRouteIds(c, routeIds, points)

	printLines()
	print u'Әскерий гарнизонға баратуғын жөнелислер'
	routeIds = getRouteIdsByDestinationId(c, askeriyGarnizonId)
	rowCount = printRoutesByRouteIds(c, routeIds, points)
	assert rowCount == 9

	printLines()
	print u'Хожан аўылға баратуғын жөнелислер'
	routeIds = getRouteIdsByDestinationId(c, xojanAwilId)
	rowCount = printRoutesByRouteIds(c, routeIds, points)
	assert rowCount == 4

	printLines()
	print u'Адай аўылға баратуғын жөнелислер'
	routeIds = getRouteIdsByDestinationId(c, adayAwilId)
	rowCount = printRoutesByRouteIds(c, routeIds, points)
	assert rowCount == 1

	printLines()
	print u'Орайлық туўыў үйинен Әскерий гарнизонға:'
	orayliqTuwiwUyiId = getDestIdByName(c, u'Орайлық туўыў үйи')
	pathIdsToOrayliqTuwiwUyi = getRouteIdsByDestinationId(c, orayliqTuwiwUyiId)
	pathIdsToAskeriyGarnizon = getRouteIdsByDestinationId(c, askeriyGarnizonId)
	intersection = set(pathIdsToOrayliqTuwiwUyi.split(',')) & set(pathIdsToAskeriyGarnizon.split(','))
	rowCount = printRoutesByRouteIds(c, ','.join(intersection), points)
	print 'Жөнелислер саны:', rowCount
	assert rowCount == 0

	printLines()
	print u'Орайлық базардан Темир Жол Вокзалына:'
	temirJolVokzali = getDestIdByName(c, u'Темир Жол Вокзалы')
	pathIdsToTemirJolVokzali = getRouteIdsByDestinationId(c, temirJolVokzali)
	pathIdsToOrayliqBazar = getRouteIdsByDestinationId(c, orayliqBazarId)
	intersection = set(pathIdsToTemirJolVokzali.split(',')) & set(pathIdsToOrayliqBazar.split(','))
	rowCount = printRoutesByRouteIds(c, ','.join(intersection), points)
	print 'Жөнелислер саны:', rowCount

	conn.commit()
	conn.close()


def getDestIdByName(cursor, name):
	cursor.execute("SELECT _id FROM destinations WHERE name = ?", [name])
	return cursor.fetchone()[0]

"""points array is indexed from 0, but _id in destinations starts from 1"""
def getAllDestinationPoints(cursor):
	cursor.execute("SELECT name FROM destinations")
	rows = cursor.fetchall()
	
	points = []	
	for row in rows:
		points.append(row[0])
	return points

def getRouteIdsByDestinationId(cursor, destId):
	cursor.execute("SELECT routeIds FROM reverseroutes WHERE destinationId = ?", [destId])
	return cursor.fetchone()[0]

def printRoutesByRouteIds(cursor, routeIds, points):
	cursor.execute("SELECT _id, type, displaynumber, pointA, pointB, pointC FROM routes WHERE _id IN ("+routeIds+")")
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