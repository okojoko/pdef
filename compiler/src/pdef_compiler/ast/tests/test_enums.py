# encoding: utf-8
import unittest
from pdef_compiler.ast.enums import *


class TestEnum(unittest.TestCase):
    def test_add_value(self):
        '''Should add to enum a new value by its name.'''
        enum = Enum('Number')
        one = enum.create_value('ONE')

        assert one.name == 'ONE'
        assert one.enum is enum

    def test_get_value(self):
        enum = Enum('Number')
        one = enum.create_value('ONE')

        assert enum.get_value('ONE') is one

    def test_validate__duplicate_values(self):
        enum = Enum('Number')
        enum.create_value('ONE')
        enum.create_value('ONE')

        errors = enum.validate()
        assert len(errors) == 1
        assert 'duplicate enum value' in errors[0]