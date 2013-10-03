# encoding: utf-8
from pdef_lang import exc


class TypeEnum(object):
    '''TypeEnum is an enumeration of all pdef type tokens.'''

    # Base value types.
    BOOL = 'bool'
    INT16 = 'int16'
    INT32 = 'int32'
    INT64 = 'int64'
    FLOAT = 'float'
    DOUBLE = 'double'
    STRING = 'string'

    # Collection types.
    LIST = 'list'
    MAP = 'map'
    SET = 'set'

    # Special data type.
    OBJECT = 'object'

    # User defined data types.
    ENUM = 'enum'
    ENUM_VALUE = 'enum_value'
    MESSAGE = 'message'
    EXCEPTION = 'exception'

    # Interface and void.
    INTERFACE = 'interface'
    VOID = 'void'

    PRIMITIVES = (BOOL, INT16, INT32, INT64, FLOAT, DOUBLE, STRING)
    DATA_TYPES = PRIMITIVES + (OBJECT, LIST, MAP, SET, ENUM, MESSAGE, EXCEPTION)
    NATIVE = PRIMITIVES + (OBJECT, VOID)


class Type(object):
    '''Type is a common class for all pdef types. These include native types, definitions,
    collections, and enum values.'''
    def __init__(self, type0):
        self.type = type0

        self.is_primitive = self.type in TypeEnum.PRIMITIVES
        self.is_data_type = self.type in TypeEnum.DATA_TYPES
        self.is_native = self.type in TypeEnum.NATIVE

        self.is_message = self.type == TypeEnum.MESSAGE
        self.is_exception = self.type == TypeEnum.EXCEPTION
        self.is_interface = self.type == TypeEnum.INTERFACE
        self.is_enum = self.type == TypeEnum.ENUM
        self.is_enum_value = self.type == TypeEnum.ENUM_VALUE

        self.is_list = self.type == TypeEnum.LIST
        self.is_set = self.type == TypeEnum.SET
        self.is_map = self.type == TypeEnum.MAP

    def __str__(self):
        return self.type

    def __repr__(self):
        return '<%s %s at %s>' % (self.__class__.__name__, self.type, hex(id(self)))


class NativeType(object):
    '''Native type singletons.'''
    BOOL = Type(TypeEnum.BOOL)
    INT16 = Type(TypeEnum.INT16)
    INT32 = Type(TypeEnum.INT32)
    INT64 = Type(TypeEnum.INT64)
    FLOAT = Type(TypeEnum.FLOAT)
    DOUBLE = Type(TypeEnum.DOUBLE)
    STRING = Type(TypeEnum.STRING)

    OBJECT = Type(TypeEnum.OBJECT)
    VOID = Type(TypeEnum.VOID)

    _BY_TYPE = {
        TypeEnum.BOOL: BOOL,
        TypeEnum.INT16: INT16,
        TypeEnum.INT32: INT32,
        TypeEnum.INT64: INT64,
        TypeEnum.FLOAT: FLOAT,
        TypeEnum.DOUBLE: DOUBLE,
        TypeEnum.STRING: STRING,
        TypeEnum.OBJECT: OBJECT,
        TypeEnum.VOID: VOID
    }

    @classmethod
    def get(cls, name):
        '''Returns a value by its type or none.'''
        return cls._BY_TYPE.get(name)


class Definition(Type):
    '''Definition is a base for user-specified types. These include messages,
    interfaces and enums.'''

    def __init__(self, type0, name, doc=None, location=None):
        super(Definition, self).__init__(type0)
        self.name = name
        self.doc = doc
        self.location = location
        self.is_exception = False
        self.module = None

    def __repr__(self):
        return '<%s %s at %s>' % (self.__class__.__name__, self.name, hex(id(self)))

    def __str__(self):
        return self.name

    def link(self, scope):
        '''Link this definition references and return a list of errors.'''
        return []

    def build(self):
        '''Build this definition after linking.'''
        pass

    def validate(self):
        '''Validate this definition and return a list of errors.'''
        return []

    def _validate_is_defined_before(self, another):
        '''Validate this definition is defined before another one.'''
        if not self.module or not another.module:
            return []

        if self.module is another.module:
            # They are in the same module.

            for def0 in self.module.definitions:
                if def0 is self:
                    return []

                if def0 is another:
                    return [exc.error(self, '%s must be defined before %s. Move it above '
                                            'in the file.', self, another)]

            raise AssertionError('Wrong module state')

        if self.module._has_import_circle(another.module):
            return [exc.error(self,
                    '%s must be referenced before %s, but their modules circularly '
                    'import each other. Move %s into another module.', self, another, self)]

        return []
