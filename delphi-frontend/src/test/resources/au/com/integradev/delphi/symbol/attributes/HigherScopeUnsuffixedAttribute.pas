unit HigherScopeUnsuffixedAttribute;

interface

type
  Foo = class(TCustomAttribute)
  end;

  TContainer = class(TObject)
  type
    FooAttribute = class(TCustomAttribute)
    end;

    [Foo]
    TBar = class(TObject)
    end;
  end;

implementation

end.