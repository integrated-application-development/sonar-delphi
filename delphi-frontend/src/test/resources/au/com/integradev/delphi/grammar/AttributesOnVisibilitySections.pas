unit AttributesOnVisibilitySections;

interface

type
  TFoo = class(TObject)
  [xyz]
  private FBar: String;

  [xyz]
  private var FBaz: String;

  [xyz]
  [assembly: xyz]
  private
    [xyz]
    var
      [xyz]
      FFlarp: String;
      FFlimflam: String;
  end;

  TBar = class(TObject)
  [xyz]
  private
    [xyz]
    var
      [xyz]

  public
    var
      FBar: String;
  end;

implementation

end.
