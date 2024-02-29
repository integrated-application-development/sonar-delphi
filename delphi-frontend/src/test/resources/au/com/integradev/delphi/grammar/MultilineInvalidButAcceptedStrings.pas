unit MultilineInvalidButAcceptedStrings;

interface

const
  Foo = '''
  ''';

  Bar = '''
 bar
  ''';

  Bar = '''''
   baz
   flarp''''';

implementation

end.