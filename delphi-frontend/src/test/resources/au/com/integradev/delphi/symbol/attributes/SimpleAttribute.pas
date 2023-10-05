unit SimpleAttribute;

interface

type
  Foo = class(TCustomAttribute)
  end;

  [Foo]
  TBar = class(TObject)
  end;

implementation

end.