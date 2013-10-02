# encoding: utf-8


class ValidatorError(object):
    def __init__(self, symbol, message, *args):
        self.symbol = symbol
        self.message = message
        if args:
            self.message = message % args

    def __repr__(self):
        return '<ValidatorError %s>' % self

    def __str__(self):
        if hasattr(self.symbol, 'location'):
            return '%s: %s' % (self.symbol.location, self.message)
        return '%s: %s' % (self.symbol, self.message)


def error(symbol, message, *args):
    return ValidatorError(symbol, message, *args)