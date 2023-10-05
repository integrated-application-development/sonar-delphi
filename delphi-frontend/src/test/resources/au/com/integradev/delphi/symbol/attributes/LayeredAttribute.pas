unit LayeredAttribute;

interface

type
  TContainer = class(TObject)
  type
    Foo = class(TCustomAttribute)
    end;
  end;

  [TContainer.Foo]
  TBar = class(TObject)
  end;

implementation

end.