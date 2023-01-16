unit EmptyClassDeclarations;

interface

type
  SomeClass = class;
  InheritedClass = class (SomeClass);
  BasicAlias = class (SomeClass);
  SealedAlias = class sealed (SomeClass);
  AbstractAlias = class abstract (SomeClass);

implementation

initialization

end.

