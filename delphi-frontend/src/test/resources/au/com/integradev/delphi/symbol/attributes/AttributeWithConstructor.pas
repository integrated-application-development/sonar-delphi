unit AttributeWithConstructor;

interface

type
  Foo = class(TCustomAttribute)
    constructor Create;
  end;

  [Foo]
  TBar = class(TObject)
  end;

implementation

end.