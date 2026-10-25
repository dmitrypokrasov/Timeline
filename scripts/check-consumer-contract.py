#!/usr/bin/env python3
"""Require real, successful external geometry contract test execution in the release consumer."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
REPORT = ROOT / 'integration/consumer/build/test-results/testReleaseUnitTest/TEST-com.example.consumer.CustomGeometryContractTest.xml'
SUITE = 'com.example.consumer.CustomGeometryContractTest'


def check(path=REPORT):
    suite = ET.parse(path).getroot()
    if suite.tag != 'testsuite' or suite.get('name') != SUITE:
        raise ValueError('Missing external CustomGeometryContractTest suite')
    cases = suite.findall('testcase')
    if not cases or int(suite.get('tests', '0')) != len(cases):
        raise ValueError('Geometry contract suite must contain real test cases')
    executed = [case for case in cases if case.find('skipped') is None]
    if not executed or int(suite.get('skipped', '0')) >= len(cases):
        raise ValueError('Geometry contract suite was entirely skipped')
    if any(case.find('failure') is not None or case.find('error') is not None for case in cases):
        raise ValueError('Geometry contract suite contains failed tests')
    if int(suite.get('failures', '0')) or int(suite.get('errors', '0')):
        raise ValueError('Geometry contract suite reports failures')
    return len(executed)


if __name__ == '__main__':
    print(f'External geometry contract: {check()} tests executed successfully.')
