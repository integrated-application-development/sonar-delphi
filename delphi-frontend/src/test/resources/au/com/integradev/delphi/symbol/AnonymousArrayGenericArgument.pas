unit AnonymousArrayGenericArgument;

interface

type
  TIndex = (iFirst, iSecond);
  TElement = Double;
  TBox<T> = class
  end;

  TFoo = class
    Values: TBox<array[TIndex] of TElement>;
  end;

implementation

end.