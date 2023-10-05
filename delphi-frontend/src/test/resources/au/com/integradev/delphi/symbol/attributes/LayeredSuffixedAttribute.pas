unit LayeredSuffixedAttribute;

interface

type
  TContainer = class(TObject)
  type
    FooAttribute = class(TCustomAttribute)
    end;
  end;

  [TContainer.FooAttribute]
  TBar = class(TObject)
  end;

implementation

end.