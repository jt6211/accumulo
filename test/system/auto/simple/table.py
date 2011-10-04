import os
import logging
import unittest
import time
import re

from TestUtils import TestUtilsMixin, ROOT, ROOT_PASSWORD, ACCUMULO_DIR

log = logging.getLogger('test.auto')

import readwrite

class TableTest(readwrite.SunnyDayTest):
    "Make a table, use it, delete it, make it again"

    order = 25

    def createTable(self, table):
        
        import tempfile
        fileno, filename = tempfile.mkstemp()
        fp = os.fdopen(fileno, "wb") 
        try:
            for i in range(0, 999999, 100):
                fp.write("%08x\n" % i)
            fp.flush()
            fp.close()
            readwrite.SunnyDayTest.createTable(self, table, filename)
        finally:
            os.unlink(filename)

    def sshell(self, msg):
        return self.rootShell(self.masterHost(), msg)

    def runTest(self):
        waitTime = 120 * self.options.rows / 1e6 + 60

        self.waitForStop(self.ingester, 90)
        self.waitForStop(self.verify(self.masterHost(), self.options.rows),
                         waitTime)
        
        #grab the table id before the table is deleted
        firstTID = self.getTableId('test_ingest')

        #verify things exist before as we expect them to before deleting
        #since after deleting we check for their absence... this will help
        #detect changes in system behavior that might require test changes

        #ensure entries in !METADATA
        out, err, code = self.sshell("table !METADATA\nscan -np\n")
        self.assert_(code == 0)
        self.assert_(re.search('^'+firstTID, out, re.MULTILINE))

        #ensure dir in hdfs
        handle = self.runOn(self.masterHost(),
                            ['hadoop', 'fs', '-ls', '%s/tables' % ACCUMULO_DIR])
        out, err = handle.communicate()
        self.assert_(out.find('%s/tables/%s' % (ACCUMULO_DIR,firstTID)) >= 0)

        #delete the table
        out, err, code = self.sshell("deletetable test_ingest\n")
        self.assert_(code == 0)
        self.shutdown_accumulo()
        self.start_accumulo()
   
        #ensure no entries in !METADATA 
        out, err, code = self.sshell("table !METADATA\nscan\n")
        self.assert_(code == 0)
        self.assert_(re.search('^'+firstTID, out, re.MULTILINE) == None)

        #ensure no dir in HDFS
        handle = self.runOn(self.masterHost(),
                            ['hadoop', 'fs', '-ls', '%s/tables' % ACCUMULO_DIR])
        out, err = handle.communicate()
        self.assert_(out.find('%s/tables/%s' % (ACCUMULO_DIR,firstTID)) < 0)

        out, err, code = self.sshell("table test_ingest\nscan -np\n")
        self.assert_(code != 0)
	self.assert_(out.find("Not in a table context.") >= 0)

        self.createTable('test_ingest')
        self.waitForStop(self.ingest(self.masterHost(), self.options.rows), 90)
        self.waitForStop(self.verify(self.masterHost(), self.options.rows),
                         waitTime)
        self.shutdown_accumulo()
        
class CreateTableSplitFile(TableTest):
    def createTable(self, table):
        import tempfile
        fileno, filename = tempfile.mkstemp()
        fp = os.fdopen(fileno, "wb")
        try:
            fp.write("a\nb\nc\nd\na\nb\nc\n")
            fp.close()
            readwrite.SunnyDayTest.createTable(self, table, filename)
        finally:
            os.unlink(filename)


def suite():
    result = unittest.TestSuite()
    result.addTest(TableTest())
    result.addTest(CreateTableSplitFile())
    return result