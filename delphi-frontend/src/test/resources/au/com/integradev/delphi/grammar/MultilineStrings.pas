unit MultilineStrings;

interface

const
  Foo = '''
  Hello this is a
  foo text
''';
  Bar = '''''
  Hello this is a
  'bar'
  text
''''';
  Baz = '''''''''
  Hello this is a
  '''baz'''
  text
''''''''';

implementation

end.