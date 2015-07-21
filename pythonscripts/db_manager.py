#!/usr/bin/env python
# -*- coding: utf-8 -*-
import sqlite3
import os
import codecs

def createDb(dbfilename):
	if (os.path.isfile(dbfilename)):
		os.remove(dbfilename)

	conn = sqlite3.connect(dbfilename)
	c = conn.cursor()


	c.execute('''CREATE TABLE destinations (id integer, name_cyr text, name_lat text)''')
	print "'destinations' table is created"

	c.execute('''CREATE TABLE routes (id integer, type integer, displaynumber integer, 
					pointA integer, pointB integer, pointC integer)''')
	print "'routes' table is created"

	c.execute('''CREATE TABLE reverseroutes (destinationId integer, routeIds text)''')
	print "'reverseroutes' table is created"

	c.execute('''CREATE TABLE reachabledestinations (destinationId integer, reachableDestinationIds text)''')
	print "'reachabledestinations' table is created"

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

		# dict with keys corresponding to destination names, 
		# and values to destination IDs
		destinations = {}

		# dict with keys corresponding to destination IDs,
		# and values to list of route IDs
		reverseroutes = {}

		# dict with keys corresponding to destination IDs,
		# and values to set of reachible destination IDs
		reachibledestinations = {}

		counter = -1
		routeIdCounter = 0

		for line in f:
			if counter == -1:
				counter = 0
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

			c.execute('''INSERT INTO routes (id, type, displaynumber, pointA, pointB, pointC)
						 VALUES (?, ?, ?, ?, ?, ?)''', \
						  (routeIdCounter, transportType, displaynumber, point_ids[0], point_ids[1], point_ids[2]))
			lastrowid = routeIdCounter
			# print 'lastrowid=',lastrowid

			for i in xrange(3):
				if not points[i]:
					continue

				# add reverse routes
				if reverseroutes.has_key(point_ids[i]):
					reverseroutes[point_ids[i]].append(lastrowid)
				else:
					reverseroutes[point_ids[i]] = [lastrowid]

				# add all three destinations to each of destinations as reachible
				if reachibledestinations.has_key(point_ids[i]):
					reachibledestinations[point_ids[i]].add(point_ids[0])
					reachibledestinations[point_ids[i]].add(point_ids[1])
				else:
					reachibledestinations[point_ids[i]] = set([point_ids[0], point_ids[1]])

				if points[2]:
					reachibledestinations[point_ids[i]].add(point_ids[2])

			routeIdCounter += 1

		destination_arr = [''] * len(destinations)
		for name in destinations.keys():
			destination_arr[destinations[name]] = name

		for idx, name in enumerate(destination_arr):
			#print name, type(name)
			c.execute('''INSERT INTO destinations (id, name_cyr, name_lat) VALUES (?, ?, ?)''', (idx, name, cyrToLatConvertor(name)))

		print "reverse routes"
		printLines()
		for destId in reverseroutes.keys():
			routeIds = map(str, reverseroutes[destId])
			c.execute('''INSERT INTO reverseroutes (destinationId, routeIds) VALUES (?, ?)''',\
							(destId, ','.join(routeIds)))
			print destId, ','.join(routeIds)

		printLines()
		print "reachable destinations"
		printLines()
		for destId in reachibledestinations.keys():
			# remove self
			reachibledestinations[destId] -= set([destId])

			reachibledestinationIds = map(str, reachibledestinations[destId])
			c.execute('''INSERT INTO reachabledestinations (destinationId, reachableDestinationIds)
						 VALUES (?, ?)''', (destId, ','.join(reachibledestinationIds)))
			print destId, ','.join(reachibledestinationIds)
			
		conn.commit()
		conn.close()

def printData(dbfilename):
	conn = sqlite3.connect(dbfilename)
	c = conn.cursor()

	c.execute("SELECT id, name_cyr, name_lat FROM destinations")

	rows = c.fetchall()
	#points = [(x[0], x[1]) for x in rows]
	#points = sorted(points)
	
	points = []	
	
	printLines()
	print "All destinations"
	printLines()

	for row in rows:
		points.append(row[1])
		print row[0], '-', row[1], '-', row[2]

	printLines()
	print "All routes"
	printLines()

	c.execute("SELECT id, type, displaynumber, pointA, pointB, pointC FROM routes")

	rows = c.fetchall()

	for row in rows:
		print row[0], ('AVTOBUS' if row[1] == 1 else 'MARSHRUTKA'), 'N%d'%row[2], \
				points[row[3]], '-', points[row[4]], \
				'-' if row[5] != -1 else '', \
				points[row[5]] if row[5] != -1 else ''

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

def printReachibleDestinations(dbfilename):
	conn = sqlite3.connect(dbfilename)
	c = conn.cursor()

	orayliqBazarId = getDestIdByName(c, u'Орайлық базар')
	askeriyGarnizonId = getDestIdByName(c, u'Әскерий гарнизон')
	xojanAwilId = getDestIdByName(c, u'Хожан аўыл')
	adayAwilId = getDestIdByName(c, u'Адай аўыл')

	points = getAllDestinationPoints(c)

	printLines()
	print u'Орайлық базарға жөнелиси бар мәнзиллер: '
	destinations = getReachibleDestIds(c, orayliqBazarId)
	printDestinationsById(points, destinations.split(','))

	printLines()
	print u'Әскерий гарнизонға жөнелиси бар мәнзиллер: '
	destinations = getReachibleDestIds(c, askeriyGarnizonId)
	printDestinationsById(points, destinations.split(','))	

	printLines()
	print u'Хожан аўылға жөнелиси бар мәнзиллер: '
	destinations = getReachibleDestIds(c, xojanAwilId)
	printDestinationsById(points, destinations.split(','))	

	printLines()
	print u'Адай аўылға жөнелиси бар мәнзиллер: '
	destinations = getReachibleDestIds(c, adayAwilId)
	printDestinationsById(points, destinations.split(','))	

	conn.commit()
	conn.close()

def getReachibleDestIds(cursor, destId):
	cursor.execute("SELECT reachableDestinationIds from reachabledestinations where destinationId = ?", (destId, ))
	return cursor.fetchone()[0]

def printDestinationsById(points, destIdList):
	destIdsAsStr = []
	for destId in destIdList:
		destIdsAsStr.append(points[int(destId)])
	print ', '.join(destIdsAsStr)

def getDestIdByName(cursor, name):
	cursor.execute("SELECT id FROM destinations WHERE name_cyr = ?", [name])
	return cursor.fetchone()[0]

def getAllDestinationPoints(cursor):
	cursor.execute("SELECT name_cyr FROM destinations")
	rows = cursor.fetchall()
	
	points = []	
	for row in rows:
		points.append(row[0])
	return points

def getRouteIdsByDestinationId(cursor, destId):
	cursor.execute("SELECT routeIds FROM reverseroutes WHERE destinationId = ?", [destId])
	return cursor.fetchone()[0]

def printRoutesByRouteIds(cursor, routeIds, points):
	cursor.execute("SELECT id, type, displaynumber, pointA, pointB, pointC FROM routes WHERE id IN ("+routeIds+")")
	rows = cursor.fetchall()

	for row in rows:
		print row[0], ('AVTOBUS' if row[1] == 1 else 'MARSHRUTKA'), 'N%d'%row[2], \
				points[row[3]], '-', points[row[4]], \
				'-' if row[5] != -1 else '', \
				points[row[5]] if row[5] != -1 else ''

	return len(rows)

def printLines():
	print "-" * 40


def cyrToLatConvertor(text):
    character_mapping_upper = {
        u'А': u"A",
        u'Ә': u"A'",
        u'Б': u"B",
        u'В': u"V",
        u'Г': u"G",
        u'Ғ': u"G'",
        u'Д': u"D",
        u'Е': u"E",
        u'Ё': u"Yo",
        u'Ж': u"J",
        u'З': u"Z",
        u'И': u"İ",
        u'Й': u"Y",
        u'К': u"K",
        u'Қ': u"Q",
        u'Л': u"L",
        u'М': u"M",
        u'Н': u"N",
        u'Ң': u"N'",
        u'О': u"O",
        u'Ө': u"O'",
        u'П': u"P",
        u'Р': u"R",
        u'С': u"S",
        u'Т': u"T",
        u'У': u"U",
        u'Ү': u"U'",
        u'Ў': u"W",
        u'Ф': u"F",
        u'Х': u"X",
        u'Ҳ': u"H",
        u'Ц': u"Ts",
        u'Ч': u"Ch",
        u'Ш': u"Sh",
        u'Щ': u"Sh",
        u'Ъ': u"",
        u'Ы': u"I",
        u'Ь': u"",
        u'Э': u"E",
        u'Ю': u"Yu",
        u'Я': u"Ya",
    }

    character_mapping_lower = {
        u'а': u"a",
        u'ә': u"a'",
        u'б': u"b",
        u'в': u"v",
        u'г': u"g",
        u'ғ': u"g'",
        u'д': u"d",
        u'е': u"e",
        u'ё': u"yo",
        u'ж': u"j",
        u'з': u"z",
        u'и': u"i",
        u'й': u"y",
        u'к': u"k",
        u'қ': u"q",
        u'л': u"l",
        u'м': u"m",
        u'н': u"n",
        u'ң': u"n'",
        u'о': u"o",
        u'ө': u"o'",
        u'п': u"p",
        u'р': u"r",
        u'с': u"s",
        u'т': u"t",
        u'у': u"u",
        u'ү': u"u'",
        u'ў': u"w",
        u'ф': u"f",
        u'х': u"x",
        u'ҳ': u"h",
        u'ц': u"ts",
        u'ч': u"ch",
        u'ш': u"sh",
        u'щ': u"sh",
        u'ъ': u"",
        u'ы': u"ı",
        u'ь': u"",
        u'э': u"e",
        u'ю': u"yu",
        u'я': u"ya",

    }

    for k, v in character_mapping_upper.iteritems():
        text = text.replace(k, v)

    for k, v in character_mapping_lower.iteritems():
        text = text.replace(k, v)

    return text

if __name__ == '__main__':
	createDb('marshrutka.db')
	fillDb('marshrutka.db', 'marshrutka.csv')
	printData('marshrutka.db')
	printReverseData('marshrutka.db')
	printReachibleDestinations('marshrutka.db')