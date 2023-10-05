unit GroupedAttributes;

interface

type
  Foo = class(TCustomAttribute)
  end;

  Baz = class(TCustomAttribute)
  end;

  [Foo, Baz]
  TBar = class(TObject)
  end;

implementation

end.