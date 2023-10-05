unit SuffixedAttribute;

interface

type
  FooAttribute = class(TCustomAttribute)
  end;

  [Foo]
  TBar = class(TObject)
  end;

implementation

end.