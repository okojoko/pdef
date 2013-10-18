# encoding: utf-8
import logging
from pdefc.ast.definitions import Definition, TypeEnum, NativeType, Located
from pdefc.ast import references


class Interface(Definition):
    '''User-defined interface.'''
    def __init__(self, name, exc=None, declared_methods=None, doc=None, location=None):
        super(Interface, self).__init__(TypeEnum.INTERFACE, name, doc=doc, location=location)

        self.exc = exc
        self.declared_methods = []

        if declared_methods:
            map(self.add_method, declared_methods)

    @property
    def exc(self):
        return self._exc.dereference()

    @exc.setter
    def exc(self, value):
        self._exc = references.reference(value)

    @property
    def methods(self):
        return self.declared_methods

    def add_method(self, method):
        '''Add a method to this interface.'''
        if method.interface:
            raise ValueError('Method is already in an interface, %s' % method)

        method.interface = self
        self.declared_methods.append(method)

        logging.debug('%s: added a method %r', self, method.name)

    def create_method(self, name, result=NativeType.VOID, is_index=False, is_post=False,
                      arg_tuples=None):
        '''Add a new method to this interface and return the method.'''
        method = Method(name, result=result, is_index=is_index, is_post=is_post)
        if arg_tuples:
            for arg_tuple in arg_tuples:
                method.create_arg(*arg_tuple)

        self.add_method(method)
        return method

    def link(self, scope):
        '''Link the base, the exception and the methods.'''
        logging.debug('Linking %s', self)
        errors = []
        errors += self._exc.link(scope)

        for method in self.declared_methods:
            errors += method.link(scope)

        return errors

    def validate(self):
        logging.debug('Validating %s', self)
        errors = []
        errors += self._validate_methods()
        errors += self._validate_exc()
        return errors

    def _validate_exc(self):
        if not self.exc:
            return []

        if not self.exc.is_exception:
            return [self._error('%s: exc must be an exception, got %s', self, self.exc)]

        return self._exc.validate()

    def _validate_methods(self):
        errors = []

        # Prevent duplicate methods.
        names = set()
        for method in self.methods:
            if method.name in names:
                errors.append(self._error('%s: duplicate method %r', self, method.name))
            names.add(method.name)

        # Prevent duplicate index methods.
        index = None
        for method in self.methods:
            if not method.is_index:
                continue

            if index:
                errors.append(self._error('%s: duplicate index method', self))
                break
            index = method

        # Validate methods.
        for method in self.methods:
            errors += method.validate()

        return errors


class Method(Located):
    '''Interface method.'''
    def __init__(self, name, result=NativeType.VOID, args=None, is_index=False, is_post=False,
                 doc=None, location=None):
        self.name = name
        self.args = []
        self.result = result

        self.is_index = is_index
        self.is_post = is_post

        self.doc = doc
        self.location = location

        self.interface = None

        if args:
            map(self.add_arg, args)

    def __str__(self):
        return self.name

    def __repr__(self):
        return '<%s %s at %s>' % (self.__class__.__name__, self.name, hex(id(self)))

    @property
    def result(self):
        return self._result.dereference()

    @result.setter
    def result(self, value):
        self._result = references.reference(value)

    @property
    def is_remote(self):
        return self.result and (not self.result.is_interface)

    def add_arg(self, arg):
        '''Append an argument to this method.'''
        if arg.method:
            raise ValueError('Argument is already in a method, %s' % arg)

        arg.method = self
        self.args.append(arg)
        logging.debug('%s: added an arg %r', self, arg.name)

    def create_arg(self, name, definition):
        '''Create a new arg and add it to this method.'''
        arg = MethodArg(name, definition)
        self.add_arg(arg)
        return arg

    def link(self, scope):
        logging.debug('Linking %s', self)
        errors = []
        errors += self._result.link(scope)

        for arg in self.args:
            errors += arg.link(scope)

        return errors

    def validate(self):
        logging.debug('Validating %s', self)
        errors = []

        # The method must have a result.
        if not self.result:
            errors.append(self._error('%s: method result required', self))
        else:
            errors += self._result.validate()

        # @post methods must be remote.
        if self.is_post and not self.is_remote:
            errors.append(self._error('%s: @post method must be remote (return a data type '
                                      'or void)', self))

        # Prevent duplicate arguments.
        names = set()
        for arg in self.args:
            if arg.name in names:
                errors.append(self._error('%s: duplicate argument %r', self, arg.name))
            names.add(arg.name)

        # Prevent form arg fields and arguments name clashes.
        for arg in self.args:
            type0 = arg.type
            if not (type0.is_message and type0.is_form):
                continue

            # It's a form.
            for field in type0.fields:
                if field.name not in names:
                    continue

                errors.append(self._error('%s: form fields clash with method args, form arg=%s',
                                          self, arg.name))
                break  # One error is enough

        for arg in self.args:
            errors += arg.validate()

        return errors


class MethodArg(Located):
    '''Single method argument.'''
    def __init__(self, name, type0):
        self.name = name
        self.type = type0
        self.method = None

    def __str__(self):
        return self.name

    def __repr__(self):
        return '<%s %s at %s>' % (self.__class__.__name__, self.name, hex(id(self)))

    @property
    def type(self):
        return self._type.dereference()

    @type.setter
    def type(self, value):
        self._type = references.reference(value)

    @property
    def fullname(self):
        return '%s.%s' % (self.method, self.name)

    def link(self, scope):
        return self._type.link(scope)

    def validate(self):
        if not self.type:
            return [self._error('%s: argument type required', self)]

        if not self.type.is_data_type:
            return [self._error('%s: argument must be a data type', self)]

        return self._type.validate()
